package com.example.doctor.controller;

import com.example.doctor.dto.DoctorLoginRequest;
import com.example.doctor.dto.PatientStatusRequest;
import com.example.doctor.dto.ScheduleChangeRequest;
import com.example.doctor.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<?> login(@RequestBody DoctorLoginRequest request) {
        String token = doctorService.login(request);
        return ResponseEntity.ok(token);
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
