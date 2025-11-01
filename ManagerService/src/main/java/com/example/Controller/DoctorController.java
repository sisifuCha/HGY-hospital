package com.example.Controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.Service.DoctorService;
import com.example.Conmon.result.Result;
import com.example.pojo.dto.DoctorDTO;
import com.example.pojo.dto.DoctorsRequestDTO;
import com.example.pojo.entity.Doctor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @GetMapping("getDoctor")
    public Result<Doctor> getDoctor(@RequestParam String doctorId) {
        return doctorService.getDoctorById(doctorId);
    }

    @PostMapping("/getDoctors")
    public Result<IPage<Doctor>> getDoctors(@RequestBody DoctorsRequestDTO doctorsRequestDTO) {
        System. out.println(doctorsRequestDTO.getFilter_value());
        return doctorService.getDoctorListWithPlus(doctorsRequestDTO.getPage(),
                doctorsRequestDTO.getNum(),doctorsRequestDTO.getFilter_name(),doctorsRequestDTO.getFilter_value());
    }

    @PostMapping("/updateDoctor")
    public Result<String> updateDoctor(@RequestBody DoctorDTO doctorDTO) {
        System.out.println("接收到医生信息更新请求"+doctorDTO.toString());
        return doctorService.updateDoctor(doctorDTO);
    }
}