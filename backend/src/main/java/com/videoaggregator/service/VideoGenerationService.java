package com.videoaggregator.service;

import com.videoaggregator.entity.GenerationTask;
import com.videoaggregator.provider.ProviderType;
import com.videoaggregator.provider.VideoGenerationRequest;

import java.util.List;

public interface VideoGenerationService {

    GenerationTask submitTask(ProviderType providerType, VideoGenerationRequest request);

    GenerationTask getTask(Long id);

    List<GenerationTask> getRecentTasks(int limit);

    void pollAndUpdateTask(Long taskId);

    String getDownloadUrl(Long taskId);
}
