package com.example.Service;

import com.example.Mapper.TriageMapper;
import com.example.dto.TriageRecommendation;
import com.example.dto.TriageRecordRow;
import com.example.dto.TriageRequest;
import com.example.dto.TriageResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class TriageServiceImpl implements TriageService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private TriageMapper triageMapper;

    @Override
    public TriageResponse getSuggestions(TriageRequest request) {
        // 1) 生成 triageId
        String triageId = "TRIAGE-" + UUID.randomUUID();

        // 2) 简单规则引擎（占位，可按你的“新逻辑”再替换）
        List<TriageRecommendation> recs = generateByRule(request);
        List<String> actions = List.of(
                "如症状加重请立即就诊",
                "建议尽快到校医院/相关科室完善检查"
        );

        // 3) 落库（复杂字段先按 JSON 字符串存 jsonb / json）
        TriageRecordRow row = new TriageRecordRow();
        row.setTriageId(triageId);
        row.setPatientId(request.getPatientId());
        row.setMode(request.getMode() == null ? "hybrid" : request.getMode());
        row.setCreatedAt(LocalDateTime.now());
        row.setSymptoms(toJson(request.getSymptoms()));
        row.setRecommendations(toJson(recs));
        row.setSuggestedActions(toJson(actions));
        triageMapper.insertTriageRecord(row);

        // 4) 查询并转换输出
        TriageRecordRow saved = triageMapper.selectByTriageId(triageId);
        return toResponse(saved);
    }

    @Override
    public List<TriageResponse> getHistory(String patientId) {
        List<TriageRecordRow> rows = triageMapper.fetchHistory(patientId);
        List<TriageResponse> out = new ArrayList<>();
        for (TriageRecordRow r : rows) {
            out.add(toResponse(r));
        }
        return out;
    }

    private static String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("JSON serialize failed", e);
        }
    }

    private static TriageResponse toResponse(TriageRecordRow row) {
        if (row == null) return null;
        TriageResponse resp = new TriageResponse();
        resp.setTriageId(row.getTriageId());
        resp.setPatientId(row.getPatientId());
        resp.setCreatedTime(row.getCreatedAt());

        // recommendations/suggestedActions 当前从 jsonb 读取到的是 JSON 字符串，这里再解析回对象
        resp.setRecommendations(parseList(row.getRecommendations(), TriageRecommendation.class));
        resp.setSuggestedActions(parseStringList(row.getSuggestedActions()));
        return resp;
    }

    private static List<String> parseStringList(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return MAPPER.readValue(json, MAPPER.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private static <T> List<T> parseList(String json, Class<T> clazz) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return MAPPER.readValue(json, MAPPER.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private static List<TriageRecommendation> generateByRule(TriageRequest request) {
        // TODO：后续按你指定的“新导诊逻辑”替换这里。
        // 这里先保证能跑通：默认推荐一个“综合门诊”。
        TriageRecommendation r = new TriageRecommendation();
        r.setDepartmentId("DEP-GENERAL");
        r.setDepartmentName("综合门诊");
        r.setConfidence(0.6);
        r.setReason("基于症状关键词的默认推荐");
        return List.of(r);
    }
}
