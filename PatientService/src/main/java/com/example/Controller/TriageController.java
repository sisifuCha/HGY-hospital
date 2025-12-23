package com.example.Controller;

import com.example.Service.TriageService;
import com.example.conmon.result.Result;
import com.example.dto.TriageRequest;
import com.example.dto.TriageResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/triage")
public class TriageController {

    @Autowired
    private TriageService triageService;

    /**
     * 智能导诊 - 根据症状获取推荐科室
     * @param request 导诊请求（包含症状、病史等）
     * @return 推荐科室列表
     */
    @PostMapping("/suggestions")
    public Result<TriageResponse> getSuggestions(@RequestBody @Valid TriageRequest request) {
        TriageResponse response = triageService.getSuggestions(request);
        return Result.success(response);
    }

    /**
     * 获取患者的导诊历史记录
     * @param patientId 患者ID
     * @return 导诊历史列表
     */
    @GetMapping("/history")
    public Result<List<TriageResponse>> getHistory(@RequestParam("patientId") String patientId) {
        List<TriageResponse> history = triageService.getHistory(patientId);
        return Result.success(history);
    }
}
