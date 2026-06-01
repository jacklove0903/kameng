package com.videoaggregator.provider.replicate;

import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
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
public class ReplicateProvider implements VideoGenerationProvider {

    private final ReplicateProperties properties;

    @Override
    public ProviderType getType() {
        return ProviderType.REPLICATE;
    }

    @Override
    public VideoGenerationResponse submit(VideoGenerationRequest request) {
        if (!isAvailable()) {
            throw new BusinessException("Replicate API Key 未配置");
        }

        Map<String, Object> body = new LinkedHashMap<>();

        // Use model name shortcut (owner/model) or version hash
        if (properties.getDefaultVersion() != null && !properties.getDefaultVersion().isBlank()) {
            body.put("version", properties.getDefaultVersion());
        }

        // Build input parameters (minimax/video-01)
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("prompt", request.getPrompt());
        input.put("prompt_optimizer", true);
        if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
            input.put("first_frame_image", request.getImageUrl());
        }

        body.put("input", input);

        String requestBody = JSONUtil.toJsonStr(body);
        log.info("Replicate submit request: {}", requestBody);

        try {
            // Replicate uses owner/model format in URL
            String url = properties.getApiUrl() + "/v1/predictions";
            if (properties.getDefaultModel() != null && !properties.getDefaultModel().isBlank()
                    && (properties.getDefaultVersion() == null || properties.getDefaultVersion().isBlank())) {
                // Use model shortcut: /v1/models/{owner}/{model}/predictions
                url = properties.getApiUrl() + "/v1/models/" + properties.getDefaultModel() + "/predictions";
            }

            HttpResponse httpResponse = HttpRequest.post(url)
                    .header(Header.AUTHORIZATION, "Bearer " + properties.getApiKey())
                    .header(Header.CONTENT_TYPE, "application/json")
                    .body(requestBody)
                    .timeout(30000)
                    .execute();

            int status = httpResponse.getStatus();
            String resultBody = httpResponse.body();
            log.info("Replicate submit response [{}]: {}", status, resultBody);

            if (status != 200 && status != 201) {
                JSONObject json = JSONUtil.parseObj(resultBody);
                throw new BusinessException(extractError(json));
            }

            JSONObject json = JSONUtil.parseObj(resultBody);
            VideoGenerationResponse response = new VideoGenerationResponse();
            response.setTaskId(json.getStr("id"));

            String taskStatus = json.getStr("status");
            if ("succeeded".equals(taskStatus)) {
                response.setStatus("SUCCESS");
                response.setVideoUrl(extractOutputUrl(json));
                response.setProgress(100);
            } else if ("failed".equals(taskStatus)) {
                response.setStatus("FAILED");
                response.setErrorMsg(json.getStr("error", "生成失败"));
            } else {
                response.setStatus("PENDING");
            }

            return response;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Replicate submit error", e);
            throw new BusinessException("Replicate 提交失败: " + e.getMessage());
        }
    }

    @Override
    public VideoGenerationResponse queryStatus(String taskId) {
        if (!isAvailable()) {
            throw new BusinessException("Replicate API Key 未配置");
        }

        try {
            HttpResponse httpResponse = HttpRequest.get(properties.getApiUrl() + "/v1/predictions/" + taskId)
                    .header(Header.AUTHORIZATION, "Bearer " + properties.getApiKey())
                    .timeout(15000)
                    .execute();

            int status = httpResponse.getStatus();
            String resultBody = httpResponse.body();
            log.info("Replicate query response [{}]: {}", status, resultBody);

            if (status != 200) {
                JSONObject json = JSONUtil.parseObj(resultBody);
                throw new BusinessException(extractError(json));
            }

            JSONObject json = JSONUtil.parseObj(resultBody);
            VideoGenerationResponse response = new VideoGenerationResponse();
            response.setTaskId(taskId);

            String taskStatus = json.getStr("status");
            switch (taskStatus != null ? taskStatus : "") {
                case "succeeded" -> {
                    response.setStatus("SUCCESS");
                    response.setVideoUrl(extractOutputUrl(json));
                    response.setProgress(100);
                }
                case "failed", "canceled" -> {
                    response.setStatus("FAILED");
                    response.setErrorMsg(json.getStr("error", "生成失败"));
                }
                case "processing", "running" -> {
                    response.setStatus("RUNNING");
                    response.setProgress(50);
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
            log.error("Replicate query error for task {}", taskId, e);
            throw new BusinessException("Replicate 查询失败: " + e.getMessage());
        }
    }

    @Override
    public boolean isAvailable() {
        return properties.getApiKey() != null && !properties.getApiKey().isEmpty();
    }

    private String extractOutputUrl(JSONObject json) {
        Object output = json.get("output");
        if (output instanceof JSONArray array && !array.isEmpty()) {
            return array.get(0).toString();
        }
        if (output instanceof String str) {
            return str;
        }
        return null;
    }

    private String extractError(JSONObject json) {
        String detail = json.getStr("detail");
        if (detail != null) return detail;
        Object error = json.get("error");
        if (error != null) return error.toString();
        return "Replicate API 调用失败";
    }
}
