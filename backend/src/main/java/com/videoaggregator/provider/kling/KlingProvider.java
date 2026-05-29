package com.videoaggregator.provider.kling;

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
public class KlingProvider implements VideoGenerationProvider {

    private final KlingProperties properties;

    @Override
    public ProviderType getType() {
        return ProviderType.KLING;
    }

    @Override
    public VideoGenerationResponse submit(VideoGenerationRequest request) {
        if (!isAvailable()) {
            throw new BusinessException("Kling API Key 未配置");
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model_name", properties.getDefaultModel());
        body.put("prompt", request.getPrompt());

        if (request.getNegativePrompt() != null && !request.getNegativePrompt().isEmpty()) {
            body.put("negative_prompt", request.getNegativePrompt());
        }
        if (request.getImageUrl() != null && !request.getImageUrl().isEmpty()) {
            body.put("image", request.getImageUrl());
        }
        if (request.getDuration() != null) {
            body.put("duration", request.getDuration());
        }
        if (request.getAspectRatio() != null) {
            body.put("aspect_ratio", mapAspectRatio(request.getAspectRatio()));
        }
        if (request.getSeed() != null) {
            body.put("seed", request.getSeed().intValue());
        }

        Map<String, Object> cfg = new LinkedHashMap<>();
        if (request.getMotionStrength() != null) {
            cfg.put("motion_strength", request.getMotionStrength());
        }
        if (!cfg.isEmpty()) {
            body.put("cfg", cfg);
        }

        try {
            HttpResponse httpResponse = HttpRequest.post(properties.getApiUrl() + "/v1/videos/image2video")
                    .header(Header.AUTHORIZATION, "Bearer " + properties.getApiKey())
                    .header(Header.CONTENT_TYPE, "application/json")
                    .body(JSONUtil.toJsonStr(body))
                    .timeout(30000)
                    .execute();

            String resultBody = httpResponse.body();
            JSONObject json = JSONUtil.parseObj(resultBody);

            if (json.getInt("code", 0) != 0) {
                String errorMsg = json.getStr("message", "Kling API 调用失败");
                log.error("Kling submit failed: {}", errorMsg);
                throw new BusinessException(errorMsg);
            }

            JSONObject data = json.getJSONObject("data");
            VideoGenerationResponse response = new VideoGenerationResponse();
            response.setTaskId(data.getStr("task_id"));
            response.setStatus("PENDING");
            return response;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Kling submit error", e);
            throw new BusinessException("Kling 提交失败: " + e.getMessage());
        }
    }

    @Override
    public VideoGenerationResponse queryStatus(String taskId) {
        if (!isAvailable()) {
            throw new BusinessException("Kling API Key 未配置");
        }

        try {
            HttpResponse httpResponse = HttpRequest.get(properties.getApiUrl() + "/v1/videos/" + taskId)
                    .header(Header.AUTHORIZATION, "Bearer " + properties.getApiKey())
                    .timeout(15000)
                    .execute();

            String resultBody = httpResponse.body();
            JSONObject json = JSONUtil.parseObj(resultBody);
            JSONObject data = json.getJSONObject("data");

            VideoGenerationResponse response = new VideoGenerationResponse();
            response.setTaskId(taskId);

            String status = data.getStr("status");
            switch (status != null ? status.toUpperCase() : "") {
                case "SUCCEED" -> {
                    response.setStatus("SUCCESS");
                    response.setVideoUrl(data.getJSONArray("videos")
                            .getJSONObject(0).getStr("url"));
                    response.setProgress(100);
                }
                case "FAILED" -> {
                    response.setStatus("FAILED");
                    response.setErrorMsg(data.getStr("fail_msg", "生成失败"));
                }
                case "PROCESSING" -> {
                    response.setStatus("RUNNING");
                    response.setProgress(data.getInt("progress", 0));
                }
                default -> {
                    response.setStatus("PENDING");
                    response.setProgress(0);
                }
            }

            return response;

        } catch (Exception e) {
            log.error("Kling query error for task {}", taskId, e);
            throw new BusinessException("Kling 查询失败: " + e.getMessage());
        }
    }

    @Override
    public boolean isAvailable() {
        return properties.getApiKey() != null && !properties.getApiKey().isEmpty();
    }

    private String mapAspectRatio(String aspectRatio) {
        return switch (aspectRatio) {
            case "16:9" -> "16:9";
            case "9:16" -> "9:16";
            case "1:1" -> "1:1";
            default -> "16:9";
        };
    }
}
