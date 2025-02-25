package com.subscore.api.utils;

import com.subscore.api.filter.JwtAuthFilter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    /**
     * 現在ログインしているユーザーのメールアドレスを取得
     */
    public static String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof JwtAuthFilter.UserInfo) {
            JwtAuthFilter.UserInfo userInfo = (JwtAuthFilter.UserInfo) auth.getPrincipal();
            return userInfo.email();
        }
        throw new RuntimeException("User not authenticated");
    }
}
