package com.example.Service;

import com.example.dto.FeedbackRequest;
import com.example.dto.FeedbackResponse;

public interface FeedbackService {
    void submitFeedback(String registrationId, FeedbackRequest request);
    FeedbackResponse getFeedback(String registrationId);
}
