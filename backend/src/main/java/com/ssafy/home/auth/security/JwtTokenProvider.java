package com.ssafy.home.auth.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.home.auth.dto.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class JwtTokenProvider {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final String jwtSecret;
    private final long expirationMinutes;
    private final ObjectMapper objectMapper;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String jwtSecret,
            @Value("${app.jwt.expiration-minutes}") long expirationMinutes,
            ObjectMapper objectMapper
    ) {
        this.jwtSecret = jwtSecret;
        this.expirationMinutes = expirationMinutes;
        this.objectMapper = objectMapper;
    }

    public String createToken(User user) {
        long nowSeconds = Instant.now().getEpochSecond();
        long expireSeconds = nowSeconds + expirationMinutes * 60;

        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", user.getEmail());
        payload.put("userId", user.getUserId());
        payload.put("email", user.getEmail());
        payload.put("nickname", user.getNickname());
        payload.put("role", user.getRole());
        payload.put("iat", nowSeconds);
        payload.put("exp", expireSeconds);

        String encodedHeader = encodeJson(header);
        String encodedPayload = encodeJson(payload);
        String unsignedToken = encodedHeader + "." + encodedPayload;
        String signature = sign(unsignedToken);

        return unsignedToken + "." + signature;
    }

    public JwtClaims parseAndValidate(String token) {
        String[] parts = token.split("\\.");

        if (parts.length != 3) {
            throw new IllegalArgumentException("JWT 형식이 올바르지 않습니다.");
        }

        String unsignedToken = parts[0] + "." + parts[1];
        String expectedSignature = sign(unsignedToken);

        if (!MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8), parts[2].getBytes(StandardCharsets.UTF_8))) {
            throw new IllegalArgumentException("JWT 서명이 올바르지 않습니다.");
        }

        Map<String, Object> payload = decodePayload(parts[1]);
        long expireSeconds = toLong(payload.get("exp"));

        if (expireSeconds < Instant.now().getEpochSecond()) {
            throw new IllegalArgumentException("JWT가 만료되었습니다.");
        }

        return new JwtClaims(
                toLong(payload.get("userId")),
                String.valueOf(payload.get("email")),
                String.valueOf(payload.get("nickname")),
                String.valueOf(payload.get("role"))
        );
    }

    private String encodeJson(Map<String, Object> value) {
        try {
            byte[] jsonBytes = objectMapper.writeValueAsBytes(value);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(jsonBytes);
        } catch (Exception error) {
            throw new IllegalStateException("JWT JSON 변환에 실패했습니다.", error);
        }
    }

    private Map<String, Object> decodePayload(String encodedPayload) {
        try {
            byte[] jsonBytes = Base64.getUrlDecoder().decode(encodedPayload);
            return objectMapper.readValue(jsonBytes, new TypeReference<>() {});
        } catch (Exception error) {
            throw new IllegalArgumentException("JWT payload를 읽을 수 없습니다.", error);
        }
    }

    private String sign(String unsignedToken) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
            mac.init(secretKeySpec);
            byte[] signatureBytes = mac.doFinal(unsignedToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBytes);
        } catch (Exception error) {
            throw new IllegalStateException("JWT 서명 생성에 실패했습니다.", error);
        }
    }

    private long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }

        return Long.parseLong(String.valueOf(value));
    }
}
