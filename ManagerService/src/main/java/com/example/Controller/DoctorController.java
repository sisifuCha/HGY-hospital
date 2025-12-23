package com.example.Controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.Service.DoctorService;
import com.example.Conmon.result.Result;
import com.example.pojo.dto.DoctorDTO;
import com.example.pojo.dto.DoctorsRequestDTO;
import com.example.pojo.entity.Department;
import com.example.pojo.entity.Doctor;
import com.example.pojo.vo.DoctorDetailVO;
import com.example.pojo.vo.FinalScheduleVO;
import com.example.pojo.vo.FinalScheduleWeekVO;
import com.example.pojo.vo.ScheduleWeekVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @GetMapping("/getDoctor")
    public Result<DoctorDetailVO> getDoctor(@RequestParam String doctorId) {
        return doctorService.getDoctorById(doctorId);
    }

    @PostMapping("/getDoctors")
    public Result<Map<String, Object>> getDoctors(@RequestBody DoctorsRequestDTO doctorsRequestDTO) {
        Result<IPage<Doctor>> result = doctorService.getDoctorListWithPlus(doctorsRequestDTO.getPage(),
                doctorsRequestDTO.getNum(),doctorsRequestDTO.getFilter_name(),doctorsRequestDTO.getFilter_value());
        
        // 构造符合API文档要求的响应结构
        Map<String, Object> response = new HashMap<>();
        response.put("doctorList", result.getData().getRecords());
        
        return Result.success(response);
    }
    @GetMapping("/options/departments")
    public Result<List<Department>> getDepartmentOptions() {
        // Service 直接返回实体列表
        List<Department> departmentList = doctorService.getDepartmentOptions();
        return Result.success(departmentList);
    }

    @PostMapping("/updateDoctor")
    public Result<String> updateDoctor(@RequestBody DoctorDTO doctorDTO) {
        System.out.println("接收到医生信息更新请求"+doctorDTO.toString());
        return doctorService.updateDoctor(doctorDTO);
    }

    @GetMapping("/getSchedules")
    public Result<FinalScheduleWeekVO> getSchedules(@RequestParam("week") Integer week, @RequestParam("departName") String departName){
        System.out.println("收到请求，周次和科室名字分别为"+week+"  "+departName);
        return doctorService.getScheduleWeek(week,departName);
    }
    
    @GetMapping("/getDoctorOptions")
    public Result<Map<String, List<Map<String, String>>>> getDoctorOptions() {
        Result<List<Map<String, String>>> result = doctorService.getDoctorOptions();
        
        // 构造符合API文档要求的响应结构
        Map<String, List<Map<String, String>>> response = new HashMap<>();
        response.put("options", result.getData());
        
        return Result.success(response);
    }
}