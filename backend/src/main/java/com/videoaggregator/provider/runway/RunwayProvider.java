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

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RunwayProvider implements VideoGenerationProvider {

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

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", properties.getDefaultModel());
        body.put("promptText", request.getPrompt());

        if (request.getImageUrl() != null && !request.getImageUrl().isEmpty()) {
            body.put("promptImage", request.getImageUrl());
        }
        if (request.getDuration() != null) {
            body.put("duration", request.getDuration());
        }
        if (request.getResolution() != null) {
            body.put("resolution", request.getResolution());
        }
        if (request.getAspectRatio() != null) {
            body.put("ratio", mapAspectRatio(request.getAspectRatio()));
        }
        if (request.getSeed() != null) {
            body.put("seed", request.getSeed().intValue());
        }

        try {
            HttpResponse httpResponse = HttpRequest.post(properties.getApiUrl() + "/v1/image_to_video")
                    .header(Header.AUTHORIZATION, "Bearer " + properties.getApiKey())
                    .header(Header.CONTENT_TYPE, "application/json")
                    .body(JSONUtil.toJsonStr(body))
                    .timeout(30000)
                    .execute();

            String resultBody = httpResponse.body();
            JSONObject json = JSONUtil.parseObj(resultBody);

            if (httpResponse.getStatus() != 200 && httpResponse.getStatus() != 201) {
                String errorMsg = json.getStr("error", "Runway API 调用失败");
                log.error("Runway submit failed: {}", errorMsg);
                throw new BusinessException(errorMsg);
            }

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
                    .timeout(15000)
                    .execute();

            String resultBody = httpResponse.body();
            JSONObject json = JSONUtil.parseObj(resultBody);

            VideoGenerationResponse response = new VideoGenerationResponse();
            response.setTaskId(taskId);

            String status = json.getStr("status");
            switch (status != null ? status.toUpperCase() : "") {
                case "SUCCEEDED" -> {
                    response.setStatus("SUCCESS");
                    response.setVideoUrl(json.getJSONArray("artifacts")
                            .getJSONObject(0).getStr("url"));
                    response.setProgress(100);
                }
                case "FAILED" -> {
                    response.setStatus("FAILED");
                    response.setErrorMsg(json.getStr("failureReason", "生成失败"));
                }
                case "RUNNING" -> {
                    response.setStatus("RUNNING");
                    response.setProgress(json.getInt("progress", 0));
                }
                default -> {
                    response.setStatus("PENDING");
                    response.setProgress(0);
                }
            }

            return response;

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
        return switch (aspectRatio) {
            case "16:9" -> "1280:768";
            case "9:16" -> "768:1280";
            case "1:1" -> "1024:1024";
            default -> "1280:768";
        };
    }
}
