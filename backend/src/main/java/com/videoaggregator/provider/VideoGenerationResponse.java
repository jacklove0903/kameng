package com.videoaggregator.provider;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class VideoGenerationResponse {

    private String taskId;
    private String status; // PENDING, RUNNING, SUCCESS, FAILED
    private String videoUrl;
    private String errorMsg;
    private BigDecimal cost;
    private Integer progress; // 0-100
}
