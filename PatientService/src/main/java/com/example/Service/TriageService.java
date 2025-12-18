package com.example.Service;

import com.example.dto.TriageRequest;
import com.example.dto.TriageResponse;
import java.util.List;

public interface TriageService {
    TriageResponse getSuggestions(TriageRequest request);
    List<TriageResponse> getHistory(String patientId);
}
