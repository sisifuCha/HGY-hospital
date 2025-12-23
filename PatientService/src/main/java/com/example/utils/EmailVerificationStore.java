package com.example.utils;

/**
 * 邮箱验证码存储 Key 构建工具类
 */
public class EmailVerificationStore {

    private static final String KEY_PREFIX = "email:verify:";

    /**
     * 构建 Redis Key
     * @param email 邮箱
     * @param scene 业务场景（如 REGISTER, RESET_PASSWORD）
     * @return Redis Key
     */
    public static String buildKey(String email, String scene) {
        return KEY_PREFIX + scene + ":" + email;
    }
}
