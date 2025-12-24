package com.example.utils;

import com.example.Mapper.PatientMapper;
import com.example.Mapper.UserMapper;
import com.example.pojo.entity.Patient;
import com.example.pojo.entity.User;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 数据迁移工具
 * 用于将现有的明文数据加密
 * 
 * 注意：此工具仅在首次部署时运行一次，之后应注释掉或删除
 */
@Slf4j
@Component
public class DataMigrationUtil implements CommandLineRunner {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PatientMapper patientMapper;

    @Autowired
    private PasswordUtil passwordUtil;

    @Autowired
    private EncryptionUtil encryptionUtil;

    // 设置为 false 禁用自动迁移，设置为 true 启用（仅首次运行时）
    private static final boolean ENABLE_MIGRATION = false;

    @Override
    public void run(String... args) {
        if (!ENABLE_MIGRATION) {
            log.info("数据迁移已禁用，如需启用请修改 DataMigrationUtil.ENABLE_MIGRATION");
            return;
        }

        log.info("开始数据迁移...");
        
        try {
            migratePasswords();
            migratePhoneNumbers();
            migrateEmails();
            migrateNames();
            migrateIdCards();
            log.info("数据迁移完成！");
            log.warn("重要提示：迁移完成后请将 DataMigrationUtil.ENABLE_MIGRATION 设置为 false");
        } catch (Exception e) {
            log.error("数据迁移失败", e);
        }
    }

    /**
     * 迁移用户密码（明文 -> 加密）
     */
    private void migratePasswords() {
        log.info("开始迁移用户密码...");
        
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_type", "PAT");
        List<User> users = userMapper.selectList(queryWrapper);
        
        int count = 0;
        for (User user : users) {
            String password = user.getUserPassword();
            // 检查密码是否已加密（加密后的密码包含 ":" 分隔符）
            if (password != null && !password.contains(":")) {
                String encryptedPassword = passwordUtil.encryptPassword(password);
                user.setUserPassword(encryptedPassword);
                userMapper.updateById(user);
                count++;
            }
        }
        
        log.info("密码迁移完成，共加密 {} 个用户密码", count);
    }

    /**
     * 迁移手机号（明文 -> 加密）
     */
    private void migratePhoneNumbers() {
        log.info("开始迁移手机号...");
        
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_type", "PAT");
        queryWrapper.isNotNull("phone_num");
        List<User> users = userMapper.selectList(queryWrapper);
        
        int count = 0;
        for (User user : users) {
            String phone = user.getUserPhone();
            if (phone != null && phone.matches("^1[3-9]\\d{9}$")) {
                // 只加密符合手机号格式的明文数据
                try {
                    String encryptedPhone = encryptionUtil.encrypt(phone);
                    user.setUserPhone(encryptedPhone);
                    userMapper.updateById(user);
                    count++;
                } catch (Exception e) {
                    log.warn("手机号加密失败: userId={}", user.getUserId(), e);
                }
            }
        }
        
        log.info("手机号迁移完成，共加密 {} 个手机号", count);
    }

    /**
     * 迁移邮箱（明文 -> 加密）
     */
    private void migrateEmails() {
        log.info("开始迁移邮箱...");
        
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_type", "PAT");
        queryWrapper.isNotNull("email");
        List<User> users = userMapper.selectList(queryWrapper);
        
        int count = 0;
        for (User user : users) {
            String email = user.getUserEmail();
            if (email != null && email.contains("@") && !email.contains("==")) {
                // 只加密符合邮箱格式且未加密的数据（加密后会有==）
                try {
                    String encryptedEmail = encryptionUtil.encrypt(email);
                    user.setUserEmail(encryptedEmail);
                    userMapper.updateById(user);
                    count++;
                } catch (Exception e) {
                    log.warn("邮箱加密失败: userId={}", user.getUserId(), e);
                }
            }
        }
        
        log.info("邮箱迁移完成，共加密 {} 个邮箱", count);
    }

    /**
     * 迁移姓名（明文 -> 加密）
     */
    private void migrateNames() {
        log.info("开始迁移姓名...");
        
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_type", "PAT");
        queryWrapper.isNotNull("name");
        List<User> users = userMapper.selectList(queryWrapper);
        
        int count = 0;
        for (User user : users) {
            String name = user.getUserName();
            // 检查是否已加密（Base64编码通常包含==或较长）
            if (name != null && name.length() < 20 && !name.contains("==")) {
                try {
                    String encryptedName = encryptionUtil.encrypt(name);
                    user.setUserName(encryptedName);
                    userMapper.updateById(user);
                    count++;
                } catch (Exception e) {
                    log.warn("姓名加密失败: userId={}", user.getUserId(), e);
                }
            }
        }
        
        log.info("姓名迁移完成，共加密 {} 个姓名", count);
    }

    /**
     * 迁移身份证号（明文 -> 加密）
     */
    private void migrateIdCards() {
        log.info("开始迁移身份证号...");
        
        QueryWrapper<Patient> queryWrapper = new QueryWrapper<>();
        queryWrapper.isNotNull("id_num");
        List<Patient> patients = patientMapper.selectList(queryWrapper);
        
        int count = 0;
        for (Patient patient : patients) {
            String idCard = patient.getIdentificationId();
            if (idCard != null && idCard.matches("^\\d{17}[0-9Xx]$")) {
                // 只加密符合身份证号格式的明文数据
                try {
                    String encryptedIdCard = encryptionUtil.encrypt(idCard);
                    patient.setIdentificationId(encryptedIdCard);
                    patientMapper.updateById(patient);
                    count++;
                } catch (Exception e) {
                    log.warn("身份证号加密失败: patientId={}", patient.getPatientId(), e);
                }
            }
        }
        
        log.info("身份证号迁移完成，共加密 {} 个身份证号", count);
    }
}
