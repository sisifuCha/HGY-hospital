package com.example.Service;

import com.example.Mapper.PatientMapper;
import com.example.pojo.dto.PatientProfileDto;
import com.example.pojo.vo.PatientProfileVo;
import com.example.utils.EncryptionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PatientProfileServiceImpl implements PatientProfileService {

    @Autowired
    private PatientMapper patientMapper;

    @Autowired
    private EncryptionUtil encryptionUtil;

    @Override
    public void uploadProfile(String patientId, PatientProfileDto profile) {
        // 加密敏感信息
        if (profile.getName() != null) {
            profile.setName(encryptionUtil.encrypt(profile.getName()));
        }
        if (profile.getPhone() != null) {
            profile.setPhone(encryptionUtil.encrypt(profile.getPhone()));
        }
        if (profile.getAddress() != null) {
            profile.setAddress(encryptionUtil.encrypt(profile.getAddress()));
        }
        patientMapper.updateProfile(patientId, profile);
    }

    @Override
    public PatientProfileVo getProfile(String patientId) {
        PatientProfileVo profile = patientMapper.findProfileById(patientId);
        if (profile != null) {
            // 解密敏感信息
            if (profile.getName() != null) {
                try {
                    profile.setName(encryptionUtil.decrypt(profile.getName()));
                } catch (Exception e) {
                    // 如果解密失败，可能是旧数据，保持原样
                }
            }
            if (profile.getPhone() != null) {
                try {
                    profile.setPhone(encryptionUtil.decrypt(profile.getPhone()));
                } catch (Exception e) {
                    // 如果解密失败，可能是旧数据，保持原样
                }
            }
            if (profile.getAddress() != null) {
                try {
                    profile.setAddress(encryptionUtil.decrypt(profile.getAddress()));
                } catch (Exception e) {
                    // 如果解密失败，可能是旧数据，保持原样
                }
            }
        }
        return profile;
    }
}

