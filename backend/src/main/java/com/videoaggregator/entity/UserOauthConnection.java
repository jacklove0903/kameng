package com.videoaggregator.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_oauth_connection")
public class UserOauthConnection {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String provider;

    private String providerUid;

    private String accessToken;

    private String refreshToken;

    private LocalDateTime tokenExpiresAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
