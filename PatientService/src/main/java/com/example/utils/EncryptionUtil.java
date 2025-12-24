package com.example.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 数据加密工具类
 * 用于敏感数据的AES加密和解密
 */
@Component
public class EncryptionUtil {

    @Value("${app.encryption.secret-key:HGY-Hospital-2025-Secret-Key-32}")
    private String secretKey;

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    /**
     * 加密数据
     * @param data 原始数据
     * @return 加密后的Base64字符串
     */
    public String encrypt(String data) {
        if (data == null || data.isEmpty()) {
            return data;
        }
        
        try {
            // 确保密钥长度为16、24或32字节
            byte[] keyBytes = getKeyBytes();
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, ALGORITHM);
            
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            
            byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("数据加密失败", e);
        }
    }

    /**
     * 解密数据
     * @param encryptedData 加密的Base64字符串
     * @return 解密后的原始数据
     */
    public String decrypt(String encryptedData) {
        if (encryptedData == null || encryptedData.isEmpty()) {
            return encryptedData;
        }
        
        try {
            byte[] keyBytes = getKeyBytes();
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, ALGORITHM);
            
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("数据解密失败", e);
        }
    }

    /**
     * 获取标准化的密钥字节数组（32字节用于AES-256）
     */
    private byte[] getKeyBytes() {
        byte[] keyBytes = new byte[32]; // AES-256需要32字节密钥
        byte[] secretBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(secretBytes, 0, keyBytes, 0, Math.min(secretBytes.length, keyBytes.length));
        return keyBytes;
    }

    /**
     * 对手机号进行脱敏显示（加密存储，显示时脱敏）
     * @param phone 手机号
     * @return 脱敏后的手机号 (如: 138****5678)
     */
    public String maskPhone(String phone) {
        if (phone == null || phone.length() < 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    /**
     * 对身份证号进行脱敏显示
     * @param idCard 身份证号
     * @return 脱敏后的身份证号 (如: 110***********1234)
     */
    public String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 18) {
            return idCard;
        }
        return idCard.substring(0, 3) + "***********" + idCard.substring(14);
    }

    /**
     * 对邮箱进行脱敏显示
     * @param email 邮箱
     * @return 脱敏后的邮箱 (如: abc***@example.com)
     */
    public String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        if (parts[0].length() <= 3) {
            return parts[0].charAt(0) + "***@" + parts[1];
        }
        return parts[0].substring(0, 3) + "***@" + parts[1];
    }

    /**
     * 对姓名进行脱敏显示
     * @param name 姓名
     * @return 脱敏后的姓名 (如: 张**)
     */
    public String maskName(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        if (name.length() == 1) {
            return name;
        }
        if (name.length() == 2) {
            return name.charAt(0) + "*";
        }
        return name.charAt(0) + "*".repeat(name.length() - 1);
    }
}
