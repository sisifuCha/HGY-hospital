package com.example.Controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.Service.DoctorService;
import com.example.Conmon.result.Result;
import com.example.pojo.dto.DoctorDTO;
import com.example.pojo.dto.DoctorsRequestDTO;
import com.example.pojo.entity.Department;
import com.example.pojo.entity.Doctor;
import com.example.pojo.vo.ScheduleWeekVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @GetMapping("/getDoctor")
    public Result<Doctor> getDoctor(@RequestParam String doctorId) {
        return doctorService.getDoctorById(doctorId);
    }

    @PostMapping("/getDoctors")
    public Result<IPage<Doctor>> getDoctors(@RequestBody DoctorsRequestDTO doctorsRequestDTO) {
        System.out.println(doctorService.getDoctorListWithPlus(doctorsRequestDTO.getPage(),
                doctorsRequestDTO.getNum(),doctorsRequestDTO.getFilter_name(),doctorsRequestDTO.getFilter_value()));
        return doctorService.getDoctorListWithPlus(doctorsRequestDTO.getPage(),
                doctorsRequestDTO.getNum(),doctorsRequestDTO.getFilter_name(),doctorsRequestDTO.getFilter_value());
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
    public Result<ScheduleWeekVO> getSchedules(@RequestParam("week") Integer week,@RequestParam("departId") String departId){
        System.out.println("收到请求，周次和科室id分别为"+week+"  "+departId);
        return doctorService.getScheduleWeek(week,departId);
    }

    public Result<String> CreateNextWeekSchedule(){
        return null;
    }
}