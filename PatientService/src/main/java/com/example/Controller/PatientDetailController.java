package com.example.Controller;

import com.example.Service.PatientDetailService;
import com.example.conmon.result.Result;
import com.example.pojo.vo.PatientDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patients")
public class PatientDetailController {

    @Autowired
    private PatientDetailService patientDetailService;

    /**
     * 获取患者详情（账号信息 + 档案聚合）
     */
    @GetMapping("/{patientId}")
    public Result<PatientDetailVo> getPatientDetail(@PathVariable String patientId) {
        PatientDetailVo detail = patientDetailService.getPatientDetail(patientId);
        if (detail == null) {
            return Result.fail(404, "患者不存在");
        }
        return Result.success(detail);
    }
}

