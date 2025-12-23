package com.example.Controller;

import com.example.Service.PatientService;
import com.example.Conmon.result.Result;
import com.example.pojo.dto.PatientPageRequest;
import com.example.pojo.vo.PatientDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class PatientController {

    @Autowired
    private PatientService patientService;

    /**
     * 分页获取患者表
     */
    @PostMapping("/admin/getPatients")
    public Result<?> getPatientList(@RequestBody PatientPageRequest pageRequest) {
        return patientService.getPatientList(pageRequest);
    }

    /**
     * 查看某个患者的详细信息
     */
    @GetMapping("/admin/getPatient")
    public Result<PatientDetailVO> getPatient(@RequestParam(required = false) String id) {
        return patientService.getPatientById(id);
    }
}