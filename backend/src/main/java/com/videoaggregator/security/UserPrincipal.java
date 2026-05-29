package com.videoaggregator.security;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserPrincipal {

    private Long userId;
    private String username;
}
