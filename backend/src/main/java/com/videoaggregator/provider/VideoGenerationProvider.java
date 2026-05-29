package com.videoaggregator.provider;

public interface VideoGenerationProvider {

    ProviderType getType();

    VideoGenerationResponse submit(VideoGenerationRequest request);

    VideoGenerationResponse queryStatus(String taskId);

    boolean isAvailable();
}
