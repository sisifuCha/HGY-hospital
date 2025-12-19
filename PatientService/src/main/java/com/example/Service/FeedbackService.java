package com.example.Service;

import com.example.pojo.dto.FeedbackRequest;
import com.example.pojo.dto.FeedbackResponse;

public interface FeedbackService {
    void submitFeedback(String registrationId, FeedbackRequest request);
    FeedbackResponse getFeedback(String registrationId);
}
