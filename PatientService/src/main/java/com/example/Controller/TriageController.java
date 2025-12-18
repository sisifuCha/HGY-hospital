package com.example.Controller;

import com.example.Service.TriageService;
import com.example.dto.TriageRequest;
import com.example.dto.TriageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/triage")
public class TriageController {

    @Autowired
    private TriageService triageService;

    @PostMapping("/suggestions")
    public ResponseEntity<TriageResponse> getSuggestions(@RequestBody TriageRequest request) {
        TriageResponse response = triageService.getSuggestions(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<TriageResponse>> getHistory(@RequestParam("patientId") String patientId) {
        List<TriageResponse> history = triageService.getHistory(patientId);
        return ResponseEntity.ok(history);
    }
}
