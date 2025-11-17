package com.example.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.Conmon.result.Result;
import com.example.pojo.dto.DoctorDTO;
import com.example.pojo.dto.DoctorsRequestDTO;
import com.example.pojo.entity.Department;
import com.example.pojo.entity.Doctor;
import com.example.pojo.vo.ScheduleWeekVO;

import javax.print.Doc;
import java.util.List;
//import com.example.pojo.entity.Doctor;

public interface DoctorService {
    /**
     * 更新医生信息
     */
    Result<String> updateDoctor(DoctorDTO doctorDTO);

    /**
     * 根据ID获取医生信息
     */
    Result<Doctor> getDoctorById(String id);

    /**
     * 分页查询医生信息
     * @param page
     * @param num
     * @param filterName
     * @param filterValue
     * @return
     */
    Result<IPage<Doctor>> getDoctorListWithPlus(int page, int num, String filterName, String filterValue);

    List<Department> getDepartmentOptions();
    Result<ScheduleWeekVO> getScheduleWeek(Integer week,String departName);
}