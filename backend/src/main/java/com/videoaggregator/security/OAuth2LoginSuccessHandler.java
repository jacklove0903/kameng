package com.videoaggregator.security;

import com.videoaggregator.entity.User;
import com.videoaggregator.entity.UserOauthConnection;
import com.videoaggregator.mapper.UserMapper;
import com.videoaggregator.mapper.UserOauthConnectionMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * OAuth2 登录成功处理器 — Phase 4 启用。
 * 流程：GitHub/Google 授权回调 → 自动注册/绑定 → 签发 JWT → 重定向到前端
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserMapper userMapper;
    private final UserOauthConnectionMapper oauthConnectionMapper;
    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oauthUser = oauthToken.getPrincipal();
        String registrationId = oauthToken.getAuthorizedClientRegistrationId(); // "github" or "google"

        String providerUid = oauthUser.getAttribute("id") != null
                ? String.valueOf(oauthUser.getAttribute("id"))
                : oauthUser.getAttribute("sub");

        String provider = registrationId.toUpperCase();
        String username = oauthUser.getAttribute("login") != null
                ? oauthUser.getAttribute("login")
                : oauthUser.getAttribute("name");
        String email = oauthUser.getAttribute("email");
        String avatarUrl = oauthUser.getAttribute("avatar_url");

        // 查找已有绑定
        UserOauthConnection connection = oauthConnectionMapper.selectOne(
                new LambdaQueryWrapper<UserOauthConnection>()
                        .eq(UserOauthConnection::getProvider, provider)
                        .eq(UserOauthConnection::getProviderUid, providerUid)
        );

        User user;
        if (connection != null) {
            user = userMapper.selectById(connection.getUserId());
        } else {
            // 自动注册
            user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setAvatarUrl(avatarUrl);
            userMapper.insert(user);

            connection = new UserOauthConnection();
            connection.setUserId(user.getId());
            connection.setProvider(provider);
            connection.setProviderUid(providerUid);
            oauthConnectionMapper.insert(connection);
        }

        // 签发 JWT
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        // 重定向到前端，携带 token
        String redirectUrl = "http://localhost:3000/auth/callback?token=" + token;
        response.sendRedirect(redirectUrl);
    }
}
