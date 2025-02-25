package com.subscore.api.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;
import java.util.Collections;

// JWTフィルター：認証トークンの検証とユーザー情報の抽出を行う
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    /**
     * JWTから抽出したユーザー情報を保持するレコードクラス
     */
    public record UserInfo(
            String email,    // ユーザーのメールアドレス
            String name,     // ユーザー名
            String picture   // プロフィール画像URL
    ) {}

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // リクエスト情報のデバッグ出力
        System.out.println("\n=== Request Debug ===");
        System.out.println("URI: " + request.getRequestURI());
        System.out.println("Method: " + request.getMethod());
        System.out.println("Auth Header: " + request.getHeader("Authorization"));

        // プリフライトリクエストの処理
        if (request.getMethod().equalsIgnoreCase("OPTIONS")) {
            filterChain.doFilter(request, response);
            return;
        }

        // ユーザー登録APIは認証をスキップ
        if (request.getRequestURI().equals("/api/users/register")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 認証ヘッダーの検証
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);  // "Bearer "の後のトークンを取得
            System.out.println("Token found: " + token.substring(0, Math.min(token.length(), 20)) + "...");

            try {
                // JWTからユーザー情報を抽出
                UserInfo userInfo = extractUserInfoFromJwt(token);
                System.out.println("Extracted user info: " + userInfo);

                // 認証情報をセキュリティコンテキストに設定
                Authentication auth = new UsernamePasswordAuthenticationToken(
                        userInfo,
                        null,
                        Collections.emptyList()  // 現時点では権限は空リスト
                );

                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(auth);
                SecurityContextHolder.setContext(context);

                filterChain.doFilter(request, response);
                return;
            } catch (Exception e) {
                System.out.println("Token processing error: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // 認証失敗時の処理
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("認証が必要です");
    }

    /**
     * JWTからユーザー情報を抽出するメソッド
     * @param token JWT文字列
     * @return 抽出されたユーザー情報
     */
    private UserInfo extractUserInfoFromJwt(String token) {
        try {
            // JWTの構造（ヘッダー.ペイロード.署名）を分割
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                throw new RuntimeException("Invalid JWT format");
            }

            // ペイロード部分をBase64デコード
            Base64.Decoder decoder = Base64.getUrlDecoder();
            String payload = new String(decoder.decode(parts[1]));
            System.out.println("JWT Payload: " + payload);

            // JSONをパースしてユーザー情報を抽出
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(payload);

            return new UserInfo(
                    jsonNode.path("email").asText(),
                    jsonNode.path("name").asText(),
                    jsonNode.path("picture").asText()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract user info from JWT: " + e.getMessage());
        }
    }
}