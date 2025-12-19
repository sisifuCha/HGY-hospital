package com.example.Mapper;

import com.example.pojo.dto.FeedbackRequest;
import com.example.pojo.dto.FeedbackResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FeedbackMapper {
    void insertFeedback(@Param("registrationId") String registrationId, @Param("request") FeedbackRequest request);
    FeedbackResponse fetchFeedback(@Param("registrationId") String registrationId);
}
