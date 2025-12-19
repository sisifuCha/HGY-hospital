package com.example.Service;

import com.example.Mapper.FeedbackMapper;
import com.example.pojo.dto.FeedbackRequest;
import com.example.pojo.dto.FeedbackResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    @Autowired
    private FeedbackMapper feedbackMapper;

    @Override
    public void submitFeedback(String registrationId, FeedbackRequest request) {
        feedbackMapper.insertFeedback(registrationId, request);
    }

    @Override
    public FeedbackResponse getFeedback(String registrationId) {
        return feedbackMapper.fetchFeedback(registrationId);
    }
}
