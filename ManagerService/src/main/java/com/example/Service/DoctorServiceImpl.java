package com.example.Service;

//import com.example.Mapper.DoctorMapper;
//import com.example.pojo.entity.Doctor;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Mapper.DoctorMapper;
import com.example.Conmon.result.Result;
import com.example.pojo.dto.DoctorDTO;
import com.example.pojo.dto.DoctorsRequestDTO;
import com.example.pojo.entity.Doctor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DoctorServiceImpl implements DoctorService {

    @Autowired
    private DoctorMapper DoctorMapper;

    @Override
    //@Transactional(rollbackFor = Exception.class)
    public Result<String> updateDoctor(DoctorDTO doctorDTO) {
        try {
            // 1. 检查医生是否存在
            Doctor existingDoctor = DoctorMapper.selectById(doctorDTO.getUserId());
            if (existingDoctor == null) {
                return Result.fail(404, "医生信息不存在");
            }

            // 2. 检查账号名是否重复
            int count = DoctorMapper.checkAccountNameExists(doctorDTO.getUserAccount(),doctorDTO.getUserId());
            if (count > 0) {
                return Result.fail(400, "账号名已存在");
            }

            // 3. DTO转Entity
            Doctor doctor = convertToEntity(doctorDTO);

            // 4. 执行更新
            int result = DoctorMapper.updateDoctor(doctor);
            if (result > 0) {
                System.out.println("医生信息更新成功，ID: {}"+ doctorDTO.getUserId());
                return Result.success("医生信息更新成功", null);
            } else {
                return Result.fail(500, "医生信息更新失败");
            }

        } catch (Exception e) {
            System.out.println("更新医生信息失败: {}"+e.getMessage());
            throw new RuntimeException("系统异常，更新失败");
        }
    }

    @Override
    public Result<Doctor> getDoctorById(String id) {
        return Result.success(DoctorMapper.selectById(id));
    }

    @Override
    public Result<IPage<Doctor>> getDoctorListWithPlus(int page, int num, String filterName, String filterValue) {
        Page<Doctor> pageParam = new Page<>(page, num);

        // 使用MyBatis-Plus的查询条件
        QueryWrapper<Doctor> queryWrapper = new QueryWrapper<>();
        if (filterValue != null && !filterValue.isEmpty()) {
            if ("clinic".equals(filterName)) {
                queryWrapper.eq("d.clinic_id", filterValue);
            } else if ("title".equals(filterName)) {
                queryWrapper.eq("d.doc_title_id", filterValue);
            }
        }
        queryWrapper.orderByDesc("d.id");

        return Result.success(DoctorMapper.selectDoctorPage(pageParam, queryWrapper));
    }

    private Doctor convertToEntity(DoctorDTO dto) {
        Doctor doctor = new Doctor();
        BeanUtils.copyProperties(dto, doctor);
        return doctor;
    }
}