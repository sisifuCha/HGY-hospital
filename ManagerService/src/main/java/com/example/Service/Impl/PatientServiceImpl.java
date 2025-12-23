package com.example.Service.Impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.example.Conmon.result.Result;
import com.example.Mapper.PatientMapper;
import com.example.Mapper.UserMapper;
import com.example.Service.PatientService;
import com.example.pojo.dto.PatientDTO;
import com.example.pojo.dto.PatientPageRequest;
import com.example.pojo.entity.Patient;
import com.example.pojo.entity.User;
import com.example.pojo.vo.PatientDetailVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PatientServiceImpl implements PatientService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PatientMapper patientMapper;

    @Override
    public Result<List<PatientDetailVO>> getPatientList(PatientPageRequest pageRequest) {
        try {
            // 解析分页参数
            Integer pageNum = pageRequest.getPageNum() != null ? pageRequest.getPageNum() : 0;
            Integer pageSize = pageRequest.getPageSize() != null ? Integer.parseInt(pageRequest.getPageSize()) : 10;
            
            // 创建分页对象
            Page<Patient> patientPage = new Page<>(pageNum, pageSize);
            
            // 先查询患者表，这样能确保只获取患者记录
            Page<Patient> patientResult = patientMapper.selectPage(patientPage, null);
            
            // 转换为VO列表
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            List<PatientDetailVO> patientList = patientResult.getRecords().stream().map(patient -> {
                PatientDetailVO vo = new PatientDetailVO();
                
                // 查询对应的用户信息
                User user = userMapper.selectById(patient.getId());
                if (user != null) {
                    vo.setId(user.getUserId());
                    vo.setName(user.getUserName());
                    vo.setSex(user.getUserGender());
                    vo.setAccount(user.getUserAccount());
                    vo.setEmail(user.getUserEmail());
                    vo.setPass(user.getUserPassword());
                    vo.setPhone_num(user.getUserPhone());
                    vo.setUser_type(user.getUserType());
                }
                
                // 设置患者信息
                if (patient.getBirth() != null) {
                    vo.setBirth(sdf.format(patient.getBirth()));
                }
                vo.setId_num(patient.getIdNum());
                vo.setMedical_insuranceid(patient.getMedicalInsuranceId());
                vo.setReimburse_id(patient.getReimburseId());
                vo.setStatus("正常"); // 默认状态
                
                return vo;
            }).collect(Collectors.toList());
            
            return Result.success(patientList);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail(500, "获取患者列表失败");
        }
    }

    @Override
    public Result<PatientDetailVO> getPatientById(String id) {
        try {
            // 先查询患者信息，确保只有患者能被查询到
            Patient patient = patientMapper.selectById(id);
            if (patient == null) {
                return Result.fail(404, "患者不存在");
            }
            
            PatientDetailVO vo = new PatientDetailVO();
            
            // 查询用户信息
            User user = userMapper.selectById(id);
            if (user != null) {
                vo.setId(user.getUserId());
                vo.setName(user.getUserName());
                vo.setSex(user.getUserGender());
                vo.setAccount(user.getUserAccount());
                vo.setEmail(user.getUserEmail());
                vo.setPass(user.getUserPassword());
                vo.setPhone_num(user.getUserPhone());
                vo.setUser_type(user.getUserType());
            }
            
            // 设置患者信息
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            if (patient.getBirth() != null) {
                vo.setBirth(sdf.format(patient.getBirth()));
            }
            vo.setId_num(patient.getIdNum());
            vo.setMedical_insuranceid(patient.getMedicalInsuranceId());
            vo.setReimburse_id(patient.getReimburseId());
            vo.setStatus("正常"); // 默认状态
            
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