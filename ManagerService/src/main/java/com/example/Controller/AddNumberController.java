package com.example.Controller;

import com.example.Conmon.result.Result;
import com.example.Service.AddNumberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AddNumberController {
    @Autowired
    private AddNumberService addNumberService;

    /**
     * 获取加号请求列表
     * @param pageSize 每页条目数量
     * @param page 页码
     * @return 加号请求列表
     */
    @GetMapping("/getAddedSource")
    public Result<Map<String, Object>> getAddedSource(@RequestParam(required = false) Integer pageSize, @RequestParam(required = false) Integer page) {
        return addNumberService.getAddedSource(pageSize, page);
    }

    /**
     * 审批加号请求
     * @param requestBody 请求参数
     * @return 审批结果
     */
    @PutMapping("/checkAddedSource")
    public Result<String> checkAddedSource(@RequestBody Map<String, String> requestBody) {
        String patientId = requestBody.get("patient_id");
        String schId = requestBody.get("sch_id");
        String status = requestBody.get("status");

        return addNumberService.checkAddedSource(patientId, schId, status);
    }
}