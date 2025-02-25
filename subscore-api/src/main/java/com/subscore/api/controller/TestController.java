package com.subscore.api.controller;

import com.subscore.api.filter.JwtAuthFilter.UserInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TestController {

    /**
     * 認証済みユーザーのテスト用エンドポイント
     * 認証情報からユーザー詳細を取得して返却
     */
    @GetMapping("/test")
    public ResponseEntity<?> test() {
        // セキュリティコンテキストから認証情報を取得
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserInfo userInfo = (UserInfo) auth.getPrincipal();

        // ユーザー情報を含むレスポンスを構築
        return ResponseEntity.ok(Map.of(
                "message", "Protected APIへのアクセスに成功しました",
                "timestamp", new Date(),
                "user", Map.of(
                        "email", userInfo.email(),
                        "name", userInfo.name(),
                        "image", userInfo.picture()
                )
        ));
    }
}