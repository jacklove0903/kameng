package com.videoaggregator.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.videoaggregator.common.exception.BusinessException;
import com.videoaggregator.entity.GenerationTask;
import com.videoaggregator.mapper.GenerationTaskMapper;
import com.videoaggregator.provider.ProviderFactory;
import com.videoaggregator.provider.ProviderType;
import com.videoaggregator.provider.VideoGenerationProvider;
import com.videoaggregator.provider.VideoGenerationRequest;
import com.videoaggregator.provider.VideoGenerationResponse;
import com.videoaggregator.service.VideoGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoGenerationServiceImpl implements VideoGenerationService {

    private final GenerationTaskMapper taskMapper;
    private final ProviderFactory providerFactory;

    @Override
    public GenerationTask submitTask(ProviderType providerType, VideoGenerationRequest request) {
        VideoGenerationProvider provider = providerFactory.getProvider(providerType);

        // 保存任务到数据库
        GenerationTask task = new GenerationTask();
        task.setProvider(providerType.name());
        task.setPrompt(request.getPrompt());
        task.setNegativePrompt(request.getNegativePrompt());
        task.setImageUrl(request.getImageUrl());
        task.setDuration(request.getDuration());
        task.setResolution(request.getResolution());
        task.setAspectRatio(request.getAspectRatio());
        task.setSeed(request.getSeed());
        task.setStatus("PENDING");
        taskMapper.insert(task);

        // 异步调用提供商 API
        asyncGenerateVideo(task.getId(), providerType, request);

        return task;
    }

    @Async
    public void asyncGenerateVideo(Long taskId, ProviderType providerType, VideoGenerationRequest request) {
        VideoGenerationProvider provider = providerFactory.getProvider(providerType);
        GenerationTask task = taskMapper.selectById(taskId);

        try {
            // 更新状态为运行中
            task.setStatus("RUNNING");
            task.setUpdatedAt(LocalDateTime.now());
            taskMapper.updateById(task);

            // 提交到提供商
            VideoGenerationResponse response = provider.submit(request);

            // 保存提供商任务 ID
            task.setProviderTaskId(response.getTaskId());
            taskMapper.updateById(task);

            // 轮询直到完成
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
        int maxRetries = 120; // 最多轮询 120 次（约 10 分钟）
        for (int i = 0; i < maxRetries; i++) {
            try {
                Thread.sleep(5000); // 每 5 秒查询一次
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
                    // 继续轮询
                    log.debug("Task {} still running, progress: {}%", taskId, response.getProgress());
                }
            }
        }

        // 超时
        GenerationTask task = taskMapper.selectById(taskId);
        task.setStatus("FAILED");
        task.setErrorMsg("生成超时");
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.updateById(task);
    }

    @Override
    public GenerationTask getTask(Long id) {
        GenerationTask task = taskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }
        return task;
    }

    @Override
    public List<GenerationTask> getRecentTasks(int limit) {
        return taskMapper.selectList(
                new LambdaQueryWrapper<GenerationTask>()
                        .orderByDesc(GenerationTask::getCreatedAt)
                        .last("LIMIT " + limit)
        );
    }

    @Override
    @Async
    public void pollAndUpdateTask(Long taskId) {
        GenerationTask task = taskMapper.selectById(taskId);
        if (task == null || task.getProviderTaskId() == null) {
            return;
        }

        VideoGenerationProvider provider = providerFactory.getProvider(
                ProviderType.valueOf(task.getProvider()));
        VideoGenerationResponse response = provider.queryStatus(task.getProviderTaskId());

        task.setStatus(response.getStatus());
        if ("SUCCESS".equals(response.getStatus())) {
            task.setVideoUrl(response.getVideoUrl());
            task.setCost(response.getCost());
        }
        if ("FAILED".equals(response.getStatus())) {
            task.setErrorMsg(response.getErrorMsg());
        }
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.updateById(task);
    }

    @Override
    public String getDownloadUrl(Long taskId) {
        GenerationTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }
        if (!"SUCCESS".equals(task.getStatus())) {
            throw new BusinessException("任务尚未完成");
        }
        return task.getVideoUrl();
    }
}
