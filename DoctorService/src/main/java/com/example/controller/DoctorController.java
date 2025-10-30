package com.example.controller;

import com.example.dto.DoctorLoginRequest;
import com.example.dto.PatientStatusRequest;
import com.example.dto.ScheduleChangeRequest;
import com.example.service.DoctorService;
import com.example.utils.Result;

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
    public SseEmitter getAddNumberNotifications(@RequestParam String docID) {
        return doctorService.getAddNumberNotifications(docID);
    }

    @PostMapping("/add_number_result")
    public ResponseEntity<?> reviewAddNumberRequest(
            @RequestParam String addNumberId,
            @RequestParam boolean approved) {
        boolean result = doctorService.reviewAddNumberRequest(addNumberId, approved);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/shifts")
    public ResponseEntity<?> getDepartmentShifts(@RequestParam String docID) {
        return ResponseEntity.ok(doctorService.getDepartmentShifts(docID));
    }

    @GetMapping("/patients")
    public ResponseEntity<?> getPatientList(@RequestParam String docID) {
        return ResponseEntity.ok(doctorService.getPatientList(docID));
    }

    @GetMapping("/register/{registerId}")
    public ResponseEntity<?> getPatientDetails(
            @RequestParam String docID,
            @PathVariable String registerId) {
        return ResponseEntity.ok(doctorService.getPatientDetails(docID, registerId));
    }

    @GetMapping(value = "/notifications", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter getSystemNotifications(@RequestParam String docID) {
        return doctorService.getSystemNotifications(docID);
    }

    @GetMapping("/{docID}/profile")
    public ResponseEntity<?> getDoctorProfile(@PathVariable String docID) {
        return ResponseEntity.ok(doctorService.getDoctorProfile(docID));
    }

    @PostMapping("/schedule_change_request")
    public ResponseEntity<?> submitScheduleChangeRequest(@RequestBody ScheduleChangeRequest request) {
        boolean result = doctorService.submitScheduleChangeRequest(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/patient/status")
    public ResponseEntity<?> updatePatientStatus(@RequestBody PatientStatusRequest request) {
        boolean result = doctorService.updatePatientStatus(request);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{doctorId}/profile")
    public ResponseEntity<?> updateDoctorProfile(
            @PathVariable String doctorId,
            @RequestBody Object profileData) {
        boolean result = doctorService.updateDoctorProfile(doctorId, profileData);
        return ResponseEntity.ok(result);
    }
}
