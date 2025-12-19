package com.example.Service;

import com.example.pojo.dto.TriageRequest;
import com.example.pojo.dto.TriageResponse;
import java.util.List;

public interface TriageService {
    TriageResponse getSuggestions(TriageRequest request);
    List<TriageResponse> getHistory(String patientId);
}
