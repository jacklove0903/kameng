package com.videoaggregator.provider.runway;

import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.videoaggregator.common.exception.BusinessException;
import com.videoaggregator.provider.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RunwayProvider implements VideoGenerationProvider {

    private static final String API_VERSION = "2024-11-06";
    // 1x1 white pixel PNG as base64 data URI, used when no image is provided
    private static final String PLACEHOLDER_IMAGE = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";

    private final RunwayProperties properties;

    @Override
    public ProviderType getType() {
        return ProviderType.RUNWAY;
    }

    @Override
    public VideoGenerationResponse submit(VideoGenerationRequest request) {
        if (!isAvailable()) {
            throw new BusinessException("Runway API Key 未配置");
        }

        // Build body exactly matching the official Node.js SDK format
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", properties.getDefaultModel());
        body.put("promptText", request.getPrompt());
        body.put("ratio", mapAspectRatio(request.getAspectRatio()));
        body.put("duration", 5);

        // promptImage is required — use placeholder when user doesn't provide one
        if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
            body.put("promptImage", request.getImageUrl());
        } else {
            body.put("promptImage", PLACEHOLDER_IMAGE);
        }

        String requestBody = JSONUtil.toJsonStr(body);
        log.info("Runway submit request body: {}", requestBody);

        try {
            HttpResponse httpResponse = HttpRequest.post(properties.getApiUrl() + "/v1/image_to_video")
                    .header(Header.AUTHORIZATION, "Bearer " + properties.getApiKey())
                    .header(Header.CONTENT_TYPE, "application/json")
                    .header("X-Runway-Version", API_VERSION)
                    .body(requestBody)
                    .timeout(60000)
                    .execute();

            int status = httpResponse.getStatus();
            String resultBody = httpResponse.body();
            log.info("Runway submit response [{}]: {}", status, resultBody);

            if (status != 200 && status != 201) {
                JSONObject json = JSONUtil.parseObj(resultBody);
                throw new BusinessException(extractError(json));
            }

            JSONObject json = JSONUtil.parseObj(resultBody);
            VideoGenerationResponse response = new VideoGenerationResponse();
            response.setTaskId(json.getStr("id"));
            response.setStatus("PENDING");
            return response;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Runway submit error", e);
            throw new BusinessException("Runway 提交失败: " + e.getMessage());
        }
    }

    @Override
    public VideoGenerationResponse queryStatus(String taskId) {
        if (!isAvailable()) {
            throw new BusinessException("Runway API Key 未配置");
        }

        try {
            HttpResponse httpResponse = HttpRequest.get(properties.getApiUrl() + "/v1/tasks/" + taskId)
                    .header(Header.AUTHORIZATION, "Bearer " + properties.getApiKey())
                    .header("X-Runway-Version", API_VERSION)
                    .timeout(15000)
                    .execute();

            int status = httpResponse.getStatus();
            String resultBody = httpResponse.body();
            log.info("Runway query response [{}]: {}", status, resultBody);

            if (status != 200) {
                JSONObject json = JSONUtil.parseObj(resultBody);
                throw new BusinessException(extractError(json));
            }

            JSONObject json = JSONUtil.parseObj(resultBody);
            VideoGenerationResponse response = new VideoGenerationResponse();
            response.setTaskId(taskId);

            String taskStatus = json.getStr("status");
            if (taskStatus == null) taskStatus = "";

            switch (taskStatus) {
                case "SUCCEEDED" -> {
                    response.setStatus("SUCCESS");
                    // output is an array of URLs
                    var output = json.get("output");
                    if (output instanceof java.util.List<?> list && !list.isEmpty()) {
                        response.setVideoUrl(list.get(0).toString());
                    }
                    response.setProgress(100);
                }
                case "FAILED" -> {
                    response.setStatus("FAILED");
                    response.setErrorMsg(json.getStr("failure", json.getStr("failureReason", "生成失败")));
                }
                case "RUNNING", "THROTTLED" -> {
                    response.setStatus("RUNNING");
                    response.setProgress(json.getInt("progress", 0));
                }
                default -> {
                    response.setStatus("PENDING");
                    response.setProgress(0);
                }
            }

            return response;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Runway query error for task {}", taskId, e);
            throw new BusinessException("Runway 查询失败: " + e.getMessage());
        }
    }

    @Override
    public boolean isAvailable() {
        return properties.getApiKey() != null && !properties.getApiKey().isEmpty();
    }

    private String mapAspectRatio(String aspectRatio) {
        if (aspectRatio == null) return "1280:720";
        return switch (aspectRatio) {
            case "16:9" -> "1280:720";
            case "9:16" -> "720:1280";
            case "1:1" -> "720:720";
            default -> "1280:720";
        };
    }

    private String extractError(JSONObject json) {
        // Handle structured error: {"error":"...","issues":[...]}
        if (json.containsKey("error")) {
            Object error = json.get("error");
            String msg = (error instanceof JSONObject) ?
                    ((JSONObject) error).getStr("message", error.toString()) :
                    error.toString();
            // Append issues if present
            var issues = json.getJSONArray("issues");
            if (issues != null && !issues.isEmpty()) {
                JSONObject firstIssue = issues.getJSONObject(0);
                String path = firstIssue.getStr("path", "");
                String issueMsg = firstIssue.getStr("message", "");
                if (!path.isEmpty() || !issueMsg.isEmpty()) {
                    msg += " (field: " + path + ", detail: " + issueMsg + ")";
                }
            }
            return msg;
        }
        return json.getStr("message", "Runway API 调用失败");
    }
}
