package com.videoaggregator.controller;

import com.videoaggregator.common.result.Result;
import com.videoaggregator.entity.GenerationTask;
import com.videoaggregator.provider.ProviderType;
import com.videoaggregator.provider.VideoGenerationRequest;
import com.videoaggregator.service.VideoGenerationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/generate")
@RequiredArgsConstructor
public class VideoGenerationController {

    private final VideoGenerationService generationService;

    @PostMapping
    public Result<GenerationTask> submit(@RequestBody @Valid SubmitRequest request) {
        VideoGenerationRequest genRequest = new VideoGenerationRequest();
        genRequest.setPrompt(request.getPrompt());
        genRequest.setNegativePrompt(request.getNegativePrompt());
        genRequest.setImageUrl(request.getImageUrl());
        genRequest.setDuration(request.getDuration());
        genRequest.setResolution(request.getResolution());
        genRequest.setAspectRatio(request.getAspectRatio());
        genRequest.setSeed(request.getSeed());
        genRequest.setMotionStrength(request.getMotionStrength());
        genRequest.setSamplingSteps(request.getSamplingSteps());

        GenerationTask task = generationService.submitTask(request.getProvider(), genRequest);
        return Result.success(task);
    }

    @GetMapping("/{id}")
    public Result<GenerationTask> getTask(@PathVariable Long id) {
        return Result.success(generationService.getTask(id));
    }

    @GetMapping("/{id}/download")
    public Result<String> download(@PathVariable Long id) {
        String url = generationService.getDownloadUrl(id);
        return Result.success(url);
    }

    @GetMapping("/recent")
    public Result<List<GenerationTask>> recent(@RequestParam(defaultValue = "20") int limit) {
        return Result.success(generationService.getRecentTasks(limit));
    }

    @Data
    public static class SubmitRequest {
        @NotNull(message = "提供商不能为空")
        private ProviderType provider;

        @NotBlank(message = "Prompt 不能为空")
        private String prompt;

        private String negativePrompt;
        private String imageUrl;
        private Integer duration = 5;
        private String resolution = "1080p";
        private String aspectRatio = "16:9";
        private Long seed;
        private Double motionStrength;
        private Integer samplingSteps;
    }
}
