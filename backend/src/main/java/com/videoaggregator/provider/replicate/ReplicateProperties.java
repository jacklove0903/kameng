package com.videoaggregator.provider.replicate;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "provider.replicate")
public class ReplicateProperties {

    private String apiKey;
    private String apiUrl;
    private String defaultModel;
    private String defaultVersion;
}
