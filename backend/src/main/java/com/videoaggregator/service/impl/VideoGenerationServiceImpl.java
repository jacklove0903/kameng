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
    private final AsyncGenerationService asyncGenerationService;

    @Override
    public GenerationTask submitTask(ProviderType providerType, VideoGenerationRequest request) {
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

        // 异步调用提供商 API（通过单独的 Bean 以确保 @Async 代理生效）
        asyncGenerationService.asyncGenerateVideo(task.getId(), providerType, request);

        return task;
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
