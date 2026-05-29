package com.videoaggregator.provider.runway;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "provider.runway")
public class RunwayProperties {

    private String apiKey;
    private String apiUrl;
    private String defaultModel;
}
