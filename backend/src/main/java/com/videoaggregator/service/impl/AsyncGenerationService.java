package com.videoaggregator.service.impl;

import com.videoaggregator.entity.GenerationTask;
import com.videoaggregator.mapper.GenerationTaskMapper;
import com.videoaggregator.provider.ProviderFactory;
import com.videoaggregator.provider.ProviderType;
import com.videoaggregator.provider.VideoGenerationProvider;
import com.videoaggregator.provider.VideoGenerationRequest;
import com.videoaggregator.provider.VideoGenerationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncGenerationService {

    private final GenerationTaskMapper taskMapper;
    private final ProviderFactory providerFactory;

    @Async
    public void asyncGenerateVideo(Long taskId, ProviderType providerType, VideoGenerationRequest request) {
        VideoGenerationProvider provider = providerFactory.getProvider(providerType);
        GenerationTask task = taskMapper.selectById(taskId);

        try {
            task.setStatus("RUNNING");
            task.setUpdatedAt(LocalDateTime.now());
            taskMapper.updateById(task);

            VideoGenerationResponse response = provider.submit(request);

            task.setProviderTaskId(response.getTaskId());
            taskMapper.updateById(task);

            pollUntilComplete(task.getId(), provider, response.getTaskId());

        } catch (Exception e) {
            log.error("Async generation failed for task {}", taskId, e);
            task.setStatus("FAILED");
            task.setErrorMsg(e.getMessage());
            task.setUpdatedAt(LocalDateTime.now());
            taskMapper.updateById(task);
        }
    }

    private void pollUntilComplete(Long taskId, VideoGenerationProvider provider, String providerTaskId) {
        int maxRetries = 120;
        for (int i = 0; i < maxRetries; i++) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            VideoGenerationResponse response = provider.queryStatus(providerTaskId);
            GenerationTask task = taskMapper.selectById(taskId);

            switch (response.getStatus()) {
                case "SUCCESS" -> {
                    task.setStatus("SUCCESS");
                    task.setVideoUrl(response.getVideoUrl());
                    task.setCost(response.getCost());
                    task.setUpdatedAt(LocalDateTime.now());
                    taskMapper.updateById(task);
                    return;
                }
                case "FAILED" -> {
                    task.setStatus("FAILED");
                    task.setErrorMsg(response.getErrorMsg());
                    task.setUpdatedAt(LocalDateTime.now());
                    taskMapper.updateById(task);
                    return;
                }
                case "RUNNING" -> {
                    task.setStatus("RUNNING");
                    task.setUpdatedAt(LocalDateTime.now());
                    taskMapper.updateById(task);
                    log.debug("Task {} still running, progress: {}%", taskId, response.getProgress());
                }
                case "PENDING" -> {
                    // Provider accepted the request but hasn't started processing yet
                    log.debug("Task {} pending", taskId);
                }
            }
        }

        // Timeout
        GenerationTask task = taskMapper.selectById(taskId);
        task.setStatus("FAILED");
        task.setErrorMsg("生成超时");
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.updateById(task);
    }
}
