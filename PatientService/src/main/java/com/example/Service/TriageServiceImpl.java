package com.example.Service;

import com.example.Mapper.TriageMapper;
import com.example.dto.TriageRequest;
import com.example.dto.TriageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TriageServiceImpl implements TriageService {

    @Autowired
    private TriageMapper triageMapper;

    @Override
    public TriageResponse getSuggestions(TriageRequest request) {
        // 调用规则引擎或机器学习模型生成建议
        return triageMapper.generateSuggestions(request);
    }

    @Override
    public List<TriageResponse> getHistory(String patientId) {
        return triageMapper.fetchHistory(patientId);
    }
}
