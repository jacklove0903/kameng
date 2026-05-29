package com.videoaggregator.provider;

import lombok.Data;

@Data
public class VideoGenerationRequest {

    private String prompt;
    private String negativePrompt;
    private String imageUrl;
    private Integer duration;
    private String resolution;
    private String aspectRatio;
    private Long seed;
    private Double motionStrength;
    private Integer samplingSteps;
}
