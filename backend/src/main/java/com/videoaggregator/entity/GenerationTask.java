package com.videoaggregator.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("generation_task")
public class GenerationTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String provider;

    private String prompt;

    private String negativePrompt;

    private String imageUrl;

    private Integer duration;

    private String resolution;

    private String aspectRatio;

    private Long seed;

    private String status;

    private String videoUrl;

    private String errorMsg;

    private BigDecimal cost;

    private String providerTaskId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
