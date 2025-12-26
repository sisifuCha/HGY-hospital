package com.example.Controller;

import com.example.Mapper.AddNumberSourceRecordMapper;
import com.example.Service.AddNumberService;
import com.example.conmon.result.Result;
import com.example.pojo.dto.AddNumberRequest;
import com.example.pojo.dto.AddNumberStatusDto;
import com.example.pojo.dto.ExtraApplyRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 患者端加号申请控制器
 */
@RestController
public class AddNumberController {

    @Autowired
    private AddNumberService addNumberService;

    @Autowired
    private AddNumberSourceRecordMapper addNumberSourceRecordMapper;

    /**
     * 提交加号申请
     * POST /api/add-number/request
     */
    @PostMapping("/api/add-number/request")
    public Result<?> submitAddNumberRequest(@RequestBody @Valid AddNumberRequest request) {
        try {
            AddNumberStatusDto dto = addNumberService.submitAddNumberRequest(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("addNumberId", dto.getPatientId() + "-" + dto.getScheduleRecordId());
            response.put("status", dto.getStatus());
            response.put("submitTime", dto.getApplyTime());
            response.put("doctorName", dto.getDoctorName());
            response.put("departmentName", dto.getDepartmentName());
            response.put("scheduleDate", dto.getScheduleDate());
            
            return Result.success("加号申请提交成功", response);
        } catch (RuntimeException e) {
            return Result.fail(409, e.getMessage());
        } catch (Exception e) {
            return Result.fail(500, "提交加号申请失败：" + e.getMessage());
        }
    }

    /**
     * 查看加号申请状态
     * GET /api/add-number/status?patientId=PAT0001&scheduleRecordId=SCH7890
     */
    @GetMapping("/api/add-number/status")
    public Result<?> getAddNumberStatus(
            @RequestParam String patientId,
            @RequestParam String scheduleRecordId) {
        try {
            AddNumberStatusDto dto = addNumberService.getAddNumberStatus(patientId, scheduleRecordId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("addNumberId", dto.getPatientId() + "-" + dto.getScheduleRecordId());
            response.put("status", dto.getStatus());
            response.put("applyTime", dto.getApplyTime());
            response.put("reasonText", dto.getReasonText());
            response.put("doctorName", dto.getDoctorName());
            response.put("departmentName", dto.getDepartmentName());
            response.put("scheduleDate", dto.getScheduleDate());
            response.put("timePeriodName", dto.getTimePeriodName());
            
            return Result.success(response);
        } catch (RuntimeException e) {
            return Result.fail(404, e.getMessage());
        } catch (Exception e) {
            return Result.fail(500, "查询加号申请状态失败：" + e.getMessage());
        }
    }

    /**
     * 查看我的加号申请历史
     * GET /api/add-number/history?patientId=PAT0001
     * GET /api/extra-apply?patientId=PAT0001 (兼容前端)
     */
    @GetMapping({"/api/add-number/history", "/api/extra-apply"})
    public Result<?> getAddNumberHistory(@RequestParam String patientId) {
        try {
            List<AddNumberStatusDto> historyList = addNumberService.getAddNumberHistory(patientId);

            Map<String, Object> response = new HashMap<>();
            response.put("patientId", patientId);
            response.put("total", historyList.size());
            response.put("items", historyList);

            return Result.success(response);
        } catch (Exception e) {
            return Result.fail(500, "查询加号申请历史失败：" + e.getMessage());
        }
    }

    /**
     * 提交加号申请（兼容前端格式）
     * POST /api/extra-apply
     */
    @PostMapping("/api/extra-apply")
    public Result<?> submitExtraApply(@RequestBody @Valid ExtraApplyRequest request) {
        try {
            // 根据 doctorId 和 appointmentDate 查找 scheduleRecordId
            String scheduleRecordId = addNumberSourceRecordMapper.findScheduleIdByDoctorAndDate(
                request.getDoctorId(),
                request.getAppointmentDate()
            );

            if (scheduleRecordId == null) {
                return Result.fail(404, "未找到该医生在指定日期的排班记录");
            }

            // 转换为内部请求格式
            AddNumberRequest internalRequest = new AddNumberRequest();
            internalRequest.setPatientId(request.getPatientId());
            internalRequest.setScheduleRecordId(scheduleRecordId);
            internalRequest.setReasonText(request.getReason());
            internalRequest.setReasonPic(request.getReasonPic());

            // 调用原有服务
            AddNumberStatusDto dto = addNumberService.submitAddNumberRequest(internalRequest);

            Map<String, Object> response = new HashMap<>();
            response.put("addNumberId", dto.getPatientId() + "-" + dto.getScheduleRecordId());
            response.put("status", dto.getStatus());
            response.put("submitTime", dto.getApplyTime());
            response.put("doctorName", dto.getDoctorName());
            response.put("departmentName", dto.getDepartmentName());
            response.put("scheduleDate", dto.getScheduleDate());

            return Result.success("加号申请提交成功", response);
        } catch (RuntimeException e) {
            return Result.fail(409, e.getMessage());
        } catch (Exception e) {
            return Result.fail(500, "提交加号申请失败：" + e.getMessage());
        }
    }
}
