package com.videoaggregator.provider.kling;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "provider.kling")
public class KlingProperties {

    private String apiKey;
    private String apiUrl;
    private String defaultModel;
}
