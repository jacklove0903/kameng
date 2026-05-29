package com.videoaggregator.provider;

import com.videoaggregator.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProviderFactory {

    private final List<VideoGenerationProvider> providers;

    private Map<ProviderType, VideoGenerationProvider> getProviderMap() {
        return providers.stream()
                .collect(Collectors.toMap(VideoGenerationProvider::getType, Function.identity()));
    }

    public VideoGenerationProvider getProvider(ProviderType type) {
        VideoGenerationProvider provider = getProviderMap().get(type);
        if (provider == null) {
            throw new BusinessException("不支持的提供商: " + type);
        }
        return provider;
    }

    public List<VideoGenerationProvider> getAllProviders() {
        return providers;
    }
}
