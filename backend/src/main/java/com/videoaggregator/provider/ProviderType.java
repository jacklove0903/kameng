package com.videoaggregator.provider;

import lombok.Getter;

@Getter
public enum ProviderType {

    RUNWAY("Runway", "https://api.dev.runwayml.com"),
    KLING("Kling", "https://api.klingai.com");

    private final String displayName;
    private final String defaultUrl;

    ProviderType(String displayName, String defaultUrl) {
        this.displayName = displayName;
        this.defaultUrl = defaultUrl;
    }
}
