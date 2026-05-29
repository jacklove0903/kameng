package com.videoaggregator.controller;

import com.videoaggregator.common.result.Result;
import com.videoaggregator.provider.ProviderFactory;
import com.videoaggregator.provider.ProviderType;
import com.videoaggregator.provider.VideoGenerationProvider;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/providers")
@RequiredArgsConstructor
public class ProviderController {

    private final ProviderFactory providerFactory;

    @GetMapping
    public Result<List<ProviderInfo>> list() {
        List<ProviderInfo> list = providerFactory.getAllProviders().stream()
                .map(p -> new ProviderInfo(
                        p.getType().name(),
                        p.getType().getDisplayName(),
                        p.isAvailable()
                ))
                .toList();
        return Result.success(list);
    }

    @GetMapping("/{type}/status")
    public Result<ProviderInfo> status(@PathVariable String type) {
        ProviderType providerType = ProviderType.valueOf(type.toUpperCase());
        VideoGenerationProvider provider = providerFactory.getProvider(providerType);
        return Result.success(new ProviderInfo(
                provider.getType().name(),
                provider.getType().getDisplayName(),
                provider.isAvailable()
        ));
    }

    @Data
    public static class ProviderInfo {
        private final String type;
        private final String displayName;
        private final boolean available;
    }
}
