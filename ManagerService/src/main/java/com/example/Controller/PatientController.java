package com.example.Controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Service.PatientService;
import com.example.Conmon.result.Result;
import com.example.pojo.dto.PatientDTO;
import com.example.pojo.vo.PatientDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/patient")
public class PatientController {

    @Autowired
    private PatientService patientService;

    /**
     * 分页获取患者列表
     */
    @GetMapping("/list")
    public Result<Page<PatientDetailVO>> getPatientListWithPlus(
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize) {
        return patientService.getPatientListWithPlus(pageNum, pageSize);
    }

    /**
     * 根据ID获取患者详情
     */
    @GetMapping("/detail/{id}")
    public Result<PatientDetailVO> getPatientById(@PathVariable String id) {
        return patientService.getPatientById(id);
    }

    /**
     * 更新患者信息
     */
    @PutMapping("/update/{id}")
    public Result<?> updatePatient(@PathVariable String id, @RequestBody PatientDTO patientDTO) {
        return patientService.updatePatient(id, patientDTO);
    }
}