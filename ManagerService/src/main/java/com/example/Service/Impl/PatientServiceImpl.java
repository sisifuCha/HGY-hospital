package com.example.Service.Impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.example.Conmon.result.Result;
import com.example.Mapper.PatientMapper;
import com.example.Mapper.UserMapper;
import com.example.Service.PatientService;
import com.example.pojo.dto.PatientDTO;
import com.example.pojo.entity.Patient;
import com.example.pojo.entity.User;
import com.example.pojo.vo.PatientDetailVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
public class PatientServiceImpl implements PatientService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PatientMapper patientMapper;

    @Override
    public Result<Page<PatientDetailVO>> getPatientListWithPlus(Integer pageNum, Integer pageSize) {
        try {
            // 创建分页对象
            Page<User> userPage = new Page<>(pageNum, pageSize);
            
            // 查询用户列表并分页
            Page<User> userResult = userMapper.selectPage(userPage, null);
            
            // 转换为VO列表
            Page<PatientDetailVO> patientPage = new Page<>();
            patientPage.setTotal(userResult.getTotal());
            patientPage.setPages(userResult.getPages());
            patientPage.setCurrent(userResult.getCurrent());
            patientPage.setSize(userResult.getSize());
            patientPage.setRecords(userResult.getRecords().stream().map(user -> {
                PatientDetailVO vo = new PatientDetailVO();
                vo.setId(user.getUserId());
                vo.setName(user.getUserName());
                vo.setSex(user.getUserGender());
                vo.setPhoneNum(user.getUserPhone());
                vo.setEmail(user.getUserEmail());
                
                // 查询患者信息
                Patient patient = patientMapper.selectById(user.getUserId());
                if (patient != null) {
                    vo.setBirth(patient.getBirth());
                    vo.setIdNum(patient.getIdNum());
                    vo.setMedicalInsuranceId(patient.getMedicalInsuranceId());
                    vo.setReimburseId(patient.getReimburseId());
                }
                
                return vo;
            }).collect(java.util.stream.Collectors.toList()));
            
            return Result.success(patientPage);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail(500, "获取患者列表失败");
        }
    }

    @Override
    public Result<PatientDetailVO> getPatientById(String id) {
        try {
            // 查询用户信息
            User user = userMapper.selectById(id);
            if (user == null) {
                return Result.fail(404, "患者不存在");
            }
            
            PatientDetailVO vo = new PatientDetailVO();
            vo.setId(user.getUserId());
            vo.setName(user.getUserName());
            vo.setSex(user.getUserGender());
            vo.setPhoneNum(user.getUserPhone());
            vo.setEmail(user.getUserEmail());
            
            // 查询患者信息
            Patient patient = patientMapper.selectById(id);
            if (patient != null) {
                vo.setBirth(patient.getBirth());
                vo.setIdNum(patient.getIdNum());
                vo.setMedicalInsuranceId(patient.getMedicalInsuranceId());
                vo.setReimburseId(patient.getReimburseId());
            }
            
            return Result.success(vo);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail(500, "获取患者详情失败");
        }
    }

    @Override
    @Transactional
    public Result<?> updatePatient(String id, PatientDTO dto) {
        try {
            // 查询用户和患者是否存在
            User user = userMapper.selectById(id);
            if (user == null) {
                return Result.fail(404, "患者不存在");
            }
            
            // 更新用户信息
            if (dto.getName() != null) user.setUserName(dto.getName());
            if (dto.getSex() != null) user.setUserGender(dto.getSex());
            if (dto.getPhoneNum() != null) user.setUserPhone(dto.getPhoneNum());
            if (dto.getEmail() != null) user.setUserEmail(dto.getEmail());
            userMapper.updateById(user);
            
            // 更新患者信息
            Patient patient = patientMapper.selectById(id);
            if (patient == null) {
                patient = new Patient();
                patient.setId(id);
            }
            
            if (dto.getBirth() != null) patient.setBirth(dto.getBirth());
            if (dto.getIdNum() != null) patient.setIdNum(dto.getIdNum());
            if (dto.getMedicalInsuranceId() != null) patient.setMedicalInsuranceId(dto.getMedicalInsuranceId());
            if (dto.getReimburseId() != null) patient.setReimburseId(dto.getReimburseId());
            
            if (patientMapper.selectById(id) == null) {
                patientMapper.insert(patient);
            } else {
                patientMapper.updateById(patient);
            }
            
            return Result.success("更新成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail(500, "更新患者信息失败");
        }
    }
}