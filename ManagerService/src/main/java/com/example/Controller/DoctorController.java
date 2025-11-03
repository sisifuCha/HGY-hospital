package com.example.Controller;

import com.example.Service.DoctorService;
import com.example.conmon.result.Result;
import com.example.pojo.dto.DoctorDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/doctor")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @PostMapping("/update")
    public Result<String> updateDoctor(@RequestBody DoctorDTO doctorDTO) {
        System.out.println("接收到医生信息更新请求"+doctorDTO.toString());
        return doctorService.updateDoctor(doctorDTO);
    }

//    @GetMapping("/{id}")
//    public Result<Doctor> getDoctor(@PathVariable String id) {
//        log.info("查询医生信息，id: {}", id);
//        return doctorService.getDoctorById(id);
//    }
}