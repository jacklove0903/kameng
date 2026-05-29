package com.videoaggregator.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器 — Phase 4 启用，当前仅预留。
 * 启用方式：在 SecurityConfig 中 addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtUtil.isTokenValid(token)) {
                Long userId = jwtUtil.getUserIdFromToken(token);
                // Phase 4: 设置 SecurityContext
                // 当前仅传递 userId 到 request attribute，供业务层使用
                request.setAttribute("userId", userId);
            }
        }
        filterChain.doFilter(request, response);
    }
}
