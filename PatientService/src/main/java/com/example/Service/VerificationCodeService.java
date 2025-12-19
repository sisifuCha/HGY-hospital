package com.example.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务
 * 负责生成、存储和验证激活验证码
 */
@Service
public class VerificationCodeService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String VERIFICATION_CODE_PREFIX = "activation:verification:";
    private static final int CODE_LENGTH = 6;
    private static final long EXPIRATION_MINUTES = 30;
    private static final SecureRandom random = new SecureRandom();

    /**
     * 生成6位随机验证码
     */
    public String generateCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    /**
     * 保存验证码到Redis，有效期30分钟
     * @param identificationId 身份证号
     * @param code 验证码
     */
    public void saveCode(String identificationId, String code) {
        String key = VERIFICATION_CODE_PREFIX + identificationId;
        redisTemplate.opsForValue().set(key, code, EXPIRATION_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * 验证验证码
     * @param identificationId 身份证号
     * @param code 用户提交的验证码
     * @return 验证是否成功
     */
    public boolean verifyCode(String identificationId, String code) {
        String key = VERIFICATION_CODE_PREFIX + identificationId;
        String savedCode = redisTemplate.opsForValue().get(key);
        return savedCode != null && savedCode.equals(code);
    }

    /**
     * 删除验证码（验证成功后）
     * @param identificationId 身份证号
     */
    public void deleteCode(String identificationId) {
        String key = VERIFICATION_CODE_PREFIX + identificationId;
        redisTemplate.delete(key);
    }

    /**
     * 获取验证码剩余有效时间（秒）
     * @param identificationId 身份证号
     * @return 剩余秒数，-1表示不存在
     */
    public long getRemainingTime(String identificationId) {
        String key = VERIFICATION_CODE_PREFIX + identificationId;
        Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return expire != null ? expire : -1;
    }
}
