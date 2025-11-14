package com.example.controller;

import com.example.dto.AddNumberDecisionRequest;
import com.example.dto.DoctorLoginRequest;
import com.example.dto.DoctorProfileDto;
import com.example.dto.DoctorProfileUpdateRequest;
import com.example.dto.PatientStatusRequest;
import com.example.dto.ScheduleChangeRequest;
import com.example.service.DoctorService;
import com.example.utils.Result;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/doctor")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> doctorLogin(@RequestBody DoctorLoginRequest request) {
        
        Result<Map<String,String>> result = doctorService.login(request);
        
        // 根据 Service 返回的业务结果，决定 HTTP 状态码
        if (result.isSuccess()) {
            // 成功，返回 200 OK
            return ResponseEntity.ok(result.toMap()); // 假设 Result 对象可以转换为 Map
        } else if (result.getCode() == 400) {
            // 输入错误，返回 400 Bad Request
            return ResponseEntity.badRequest().body(result.toMap());
        } else if (result.getCode() == 401) {
            // 鉴权失败，返回 401 Unauthorized
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result.toMap());
        } else {
            // 系统错误，返回 500 Internal Server Error
            return ResponseEntity.internalServerError().body(result.toMap());
        }
    }

    @GetMapping(value = "/add_number_notify_doctor", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter getAddNumberNotifications(@RequestParam String docId) {
        return doctorService.getAddNumberNotifications(docId);
    }

    @PostMapping("/add_number_result")
    public ResponseEntity<Map<String, Object>> reviewAddNumberRequest(@RequestBody AddNumberDecisionRequest request) {
        Result<Void> result = doctorService.reviewAddNumberRequest(request);
        return toResponseEntity(result);
    }

    @GetMapping("/shifts")
    public ResponseEntity<Map<String, Object>> getDepartmentShifts(@RequestParam String docId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("shifts", doctorService.getDepartmentShifts(docId));
        return ResponseEntity.ok(Result.success(payload).toMap());
    }

    @GetMapping("/selfshifts")
    public ResponseEntity<Map<String, Object>> getSelfShifts(@RequestParam String docId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("shifts", doctorService.getSelfShifts(docId));
        return ResponseEntity.ok(Result.success(payload).toMap());
    }

    @GetMapping("/patients")
    public ResponseEntity<Map<String, Object>> getPatientList(@RequestParam String docId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("patients", doctorService.getPatientList(docId));
        return ResponseEntity.ok(Result.success(payload).toMap());
    }

    @GetMapping("/register/{registerId}")
    public ResponseEntity<Map<String, Object>> getPatientDetails(
            @RequestParam String docId,
            @PathVariable String registerId) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("records", doctorService.getPatientDetails(docId, registerId));
            return ResponseEntity.ok(Result.success(payload).toMap());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Result.fail(400, ex.getMessage()).toMap());
        }
    }

    @GetMapping(value = "/notifications", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter getSystemNotifications(@RequestParam String docId) {
        return doctorService.getSystemNotifications(docId);
    }

    @GetMapping("/{docId}/profile")
    public ResponseEntity<Map<String, Object>> getDoctorProfile(@PathVariable String docId) {
        try {
            DoctorProfileDto profile = doctorService.getDoctorProfile(docId);
            Map<String, Object> payload = new HashMap<>();
            payload.put("doctor", profile);
            return ResponseEntity.ok(Result.success(payload).toMap());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Result.fail(404, ex.getMessage()).toMap());
        }
    }

    @PostMapping("/schedule_change_request")
    public ResponseEntity<Map<String, Object>> submitScheduleChangeRequest(@RequestBody ScheduleChangeRequest request) {
        Result<Void> result = doctorService.submitScheduleChangeRequest(request);
        return toResponseEntity(result);
    }

    @PostMapping("/patient/status")
    public ResponseEntity<Map<String, Object>> updatePatientStatus(@RequestBody PatientStatusRequest request) {
        Result<Void> result = doctorService.updatePatientStatus(request);
        return toResponseEntity(result);
    }

    @PutMapping("/{doctorId}/profile")
    public ResponseEntity<Map<String, Object>> updateDoctorProfile(
            @PathVariable String doctorId,
            @RequestBody DoctorProfileUpdateRequest profileData) {
        Result<Void> result = doctorService.updateDoctorProfile(doctorId, profileData);
        return toResponseEntity(result);
    }

    private ResponseEntity<Map<String, Object>> toResponseEntity(Result<?> result) {
        HttpStatus status;
        switch (result.getCode()) {
            case 200:
                status = HttpStatus.OK;
                break;
            case 400:
                status = HttpStatus.BAD_REQUEST;
                break;
            case 401:
                status = HttpStatus.UNAUTHORIZED;
                break;
            case 403:
                status = HttpStatus.FORBIDDEN;
                break;
            case 404:
                status = HttpStatus.NOT_FOUND;
                break;
            default:
                status = HttpStatus.INTERNAL_SERVER_ERROR;
                break;
        }
        return ResponseEntity.status(status).body(result.toMap());
    }
}
