package com.example.Controller;

import com.example.Service.PatientProfileService;
import com.example.conmon.result.Result;
import com.example.pojo.dto.PatientProfileDto;
import com.example.pojo.vo.PatientProfileVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patients")
public class PatientProfileController {

    @Autowired
    private PatientProfileService patientProfileService;

    @PostMapping("/{patientId}/profile")
    public Result<?> uploadProfile(@PathVariable String patientId, @RequestBody PatientProfileDto profile) {
        patientProfileService.uploadProfile(patientId, profile);
        return Result.success("档案上传成功");
    }

    @GetMapping("/{patientId}/profile")
    public Result<PatientProfileVo> getProfile(@PathVariable String patientId) {
        PatientProfileVo profile = patientProfileService.getProfile(patientId);
        if (profile == null) {
            return Result.error("患者不存在");
        }
        return Result.success(profile);
    }
}

