package com.example.utils;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Date;

public class JwtUtil {

    // 安全密钥（生产环境中应从配置文件中读取）
    private static final String SECRET_KEY = "114514-i-love-liu-duo-forever-until-the-end-of-the-world";
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; // 24小时
    private static final String USER_ROLE = "admin";

    private static SecretKey getSigningKey() {
        // 确保密钥长度足够（至少256位）
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    public static String generateToken() {
        return Jwts.builder()
                .header().add("typ", "JWT").and()
                .subject(USER_ROLE)
                .issuer("ManagerEnd")
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey()) // 修正：传入SecretKey对象而非字符串
                .compact();
    }

    public static boolean parseJWT(String token) {
        try {
            Jws<Claims> jws = Jwts.parser()
                    .verifyWith(getSigningKey()) // 同样需要修正这里
                    .build()
                    .parseSignedClaims(token);
            if (jws.getPayload().getSubject().equals(USER_ROLE)) {
                return true;
            }
        } catch (JwtException | IllegalArgumentException e) {
            throw new RuntimeException("Invalid JWT", e);
        }
        return false;
    }
}