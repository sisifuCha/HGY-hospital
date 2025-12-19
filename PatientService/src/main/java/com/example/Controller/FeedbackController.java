package com.example.Controller;

import com.example.Service.FeedbackService;
import com.example.dto.FeedbackRequest;
import com.example.dto.FeedbackResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/registrations")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @PostMapping("/{registrationId}/feedback")
    public ResponseEntity<String> submitFeedback(@PathVariable String registrationId, @RequestBody FeedbackRequest request) {
        feedbackService.submitFeedback(registrationId, request);
        return ResponseEntity.ok("Feedback submitted successfully");
    }

    @GetMapping("/{registrationId}/feedback")
    public ResponseEntity<FeedbackResponse> getFeedback(@PathVariable String registrationId) {
        FeedbackResponse response = feedbackService.getFeedback(registrationId);
        return ResponseEntity.ok(response);
    }
}
