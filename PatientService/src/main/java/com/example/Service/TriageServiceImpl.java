package com.example.Service;

import com.example.Mapper.TriageMapper;
import com.example.dto.TriageRecommendation;
import com.example.dto.TriageRecordRow;
import com.example.dto.TriageRequest;
import com.example.dto.TriageResponse;
import com.example.pojo.entity.Department;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TriageServiceImpl implements TriageService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private TriageMapper triageMapper;

    /**
     * 症状关键词 -> 科室名称映射规则
     * key: 科室名称关键词（用于模糊匹配数据库中的科室）
     * value: 相关症状关键词列表
     */
    private static final Map<String, List<String>> SYMPTOM_DEPARTMENT_RULES = new LinkedHashMap<>();

    static {
        // 内科相关
        SYMPTOM_DEPARTMENT_RULES.put("内科", Arrays.asList(
                "发热", "发烧", "感冒", "咳嗽", "头痛", "头晕", "乏力", "疲劳",
                "胸闷", "心悸", "气短", "呼吸困难", "腹痛", "腹泻", "便秘",
                "恶心", "呕吐", "食欲不振", "消化不良", "胃痛", "胃胀",
                "高血压", "低血压", "贫血", "糖尿病", "甲亢", "甲减"
        ));

        // 外科相关
        SYMPTOM_DEPARTMENT_RULES.put("外科", Arrays.asList(
                "外伤", "骨折", "扭伤", "摔伤", "割伤", "擦伤", "烫伤", "烧伤",
                "肿块", "包块", "疝气", "阑尾炎", "胆囊炎", "肠梗阻",
                "腰痛", "背痛", "关节痛", "骨痛", "肌肉拉伤"
        ));

        // 皮肤科相关
        SYMPTOM_DEPARTMENT_RULES.put("皮肤", Arrays.asList(
                "皮疹", "红疹", "荨麻疹", "湿疹", "过敏", "瘙痒", "痒",
                "痤疮", "青春痘", "粉刺", "脱发", "掉发", "头皮屑",
                "皮肤干燥", "皮肤红肿", "水泡", "脓疱", "癣", "疣"
        ));

        // 眼科相关
        SYMPTOM_DEPARTMENT_RULES.put("眼", Arrays.asList(
                "眼睛", "视力", "近视", "远视", "散光", "眼痛", "眼红",
                "红眼", "眼干", "眼涩", "流泪", "眼屎", "结膜炎",
                "眼睛痒", "眼睛肿", "飞蚊症", "白内障", "青光眼"
        ));

        // 耳鼻喉科相关
        SYMPTOM_DEPARTMENT_RULES.put("耳鼻", Arrays.asList(
                "耳朵", "耳痛", "耳鸣", "听力", "耳聋", "中耳炎",
                "鼻塞", "流鼻涕", "鼻炎", "鼻窦炎", "打喷嚏", "鼻出血",
                "喉咙", "咽喉", "嗓子", "声音嘶哑", "扁桃体", "咽炎"
        ));

        // 口腔科相关
        SYMPTOM_DEPARTMENT_RULES.put("口腔", Arrays.asList(
                "牙痛", "牙齿", "牙龈", "牙龈出血", "蛀牙", "龋齿",
                "口腔溃疡", "口臭", "智齿", "牙周炎", "牙齿松动"
        ));

        // 妇科相关
        SYMPTOM_DEPARTMENT_RULES.put("妇", Arrays.asList(
                "月经", "例假", "痛经", "闭经", "白带", "阴道",
                "乳房", "乳腺", "妇科", "宫颈", "子宫", "卵巢"
        ));

        // 泌尿科相关
        SYMPTOM_DEPARTMENT_RULES.put("泌尿", Arrays.asList(
                "尿频", "尿急", "尿痛", "血尿", "尿道", "膀胱",
                "肾结石", "尿路感染", "前列腺", "排尿困难"
        ));

        // 神经科/精神科相关
        SYMPTOM_DEPARTMENT_RULES.put("神经", Arrays.asList(
                "失眠", "睡眠", "焦虑", "抑郁", "紧张", "情绪",
                "头痛", "偏头痛", "眩晕", "手抖", "麻木", "癫痫"
        ));

        // 骨科相关
        SYMPTOM_DEPARTMENT_RULES.put("骨", Arrays.asList(
                "骨折", "脱臼", "关节", "膝盖", "肩膀", "颈椎", "腰椎",
                "腰间盘", "骨质疏松", "风湿", "类风湿", "痛风"
        ));
    }

    @Override
    public TriageResponse getSuggestions(TriageRequest request) {
        // 1) 生成 triageId
        String triageId = "TRIAGE-" + UUID.randomUUID();

        // 2) 基于规则引擎匹配症状 -> 科室
        List<TriageRecommendation> recs = generateByRule(request);

        // 3) 根据症状详情生成建议行动
        List<String> actions = generateActions(request);

        // 4) 落库（复杂字段按 JSON 字符串存 jsonb）
        TriageRecordRow row = new TriageRecordRow();
        row.setTriageId(triageId);
        row.setPatientId(request.getPatientId());
        row.setMode(request.getMode() == null ? "rule" : request.getMode());
        row.setCreatedAt(LocalDateTime.now());
        row.setSymptoms(toJson(request.getSymptoms()));
        row.setRecommendations(toJson(recs));
        row.setSuggestedActions(toJson(actions));
        triageMapper.insertTriageRecord(row);

        // 5) 查询并转换输出
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

    /**
     * 基于规则的症状-科室匹配
     */
    private List<TriageRecommendation> generateByRule(TriageRequest request) {
        List<String> symptoms = request.getSymptoms();
        if (symptoms == null || symptoms.isEmpty()) {
            return getDefaultRecommendation();
        }

        // 获取数据库中所有子科室
        List<Department> allDepartments = triageMapper.findAllSubDepartments();
        if (allDepartments == null || allDepartments.isEmpty()) {
            log.warn("No departments found in database");
            return getDefaultRecommendation();
        }

        // 计算每个科室的匹配分数
        Map<String, MatchResult> matchResults = new LinkedHashMap<>();

        for (Map.Entry<String, List<String>> rule : SYMPTOM_DEPARTMENT_RULES.entrySet()) {
            String deptKeyword = rule.getKey();
            List<String> symptomKeywords = rule.getValue();

            // 计算匹配的症状数量
            List<String> matchedSymptoms = new ArrayList<>();
            for (String symptom : symptoms) {
                for (String keyword : symptomKeywords) {
                    if (symptom.contains(keyword) || keyword.contains(symptom)) {
                        matchedSymptoms.add(symptom + "->" + keyword);
                        break;
                    }
                }
            }

            if (!matchedSymptoms.isEmpty()) {
                // 在数据库科室中查找匹配的科室
                for (Department dept : allDepartments) {
                    if (dept.getName().contains(deptKeyword)) {
                        MatchResult existing = matchResults.get(dept.getId());
                        if (existing == null) {
                            existing = new MatchResult(dept.getId(), dept.getName());
                            matchResults.put(dept.getId(), existing);
                        }
                        existing.addMatches(matchedSymptoms);
                    }
                }
            }
        }

        // 转换为推荐结果并排序
        List<TriageRecommendation> recommendations = new ArrayList<>();
        int totalSymptoms = symptoms.size();

        for (MatchResult result : matchResults.values()) {
            TriageRecommendation rec = new TriageRecommendation();
            rec.setDepartmentId(result.departmentId);
            rec.setDepartmentName(result.departmentName);
            // 置信度 = 匹配症状数 / 总症状数
            double confidence = Math.min(0.95, (double) result.matchCount / totalSymptoms);
            rec.setConfidence(Math.round(confidence * 100.0) / 100.0);
            rec.setReason("匹配症状: " + String.join("、", result.matchedKeywords));
            recommendations.add(rec);
        }

        // 按置信度排序
        recommendations.sort((a, b) -> Double.compare(b.getConfidence(), a.getConfidence()));

        // 最多返回3个推荐
        if (recommendations.size() > 3) {
            recommendations = recommendations.subList(0, 3);
        }

        // 如果没有匹配结果，返回默认推荐
        if (recommendations.isEmpty()) {
            return getDefaultRecommendation();
        }

        return recommendations;
    }

    /**
     * 默认推荐（无法匹配时使用）
     */
    private List<TriageRecommendation> getDefaultRecommendation() {
        // 尝试从数据库找一个内科或全科
        List<Department> departments = triageMapper.findAllSubDepartments();
        String defaultDeptId = "GENERAL";
        String defaultDeptName = "全科门诊";

        if (departments != null) {
            for (Department dept : departments) {
                if (dept.getName().contains("内科") || dept.getName().contains("全科")) {
                    defaultDeptId = dept.getId();
                    defaultDeptName = dept.getName();
                    break;
                }
            }
        }

        TriageRecommendation rec = new TriageRecommendation();
        rec.setDepartmentId(defaultDeptId);
        rec.setDepartmentName(defaultDeptName);
        rec.setConfidence(0.5);
        rec.setReason("未能精确匹配症状，建议先到全科/内科就诊");
        return List.of(rec);
    }

    /**
     * 根据发热温度和持续时间生成建议行动
     */
    private List<String> generateActions(TriageRequest request) {
        List<String> actions = new ArrayList<>();

        // 检查体温
        if (request.getTemperature() != null) {
            if (request.getTemperature() >= 39.0) {
                actions.add("体温较高（" + request.getTemperature() + "℃），建议尽快就诊");
            } else if (request.getTemperature() >= 38.0) {
                actions.add("有发热症状，注意多休息、多喝水");
            }
        }

        // 检查持续时间
        if (request.getDurationDays() != null) {
            if (request.getDurationDays() >= 7) {
                actions.add("症状已持续" + request.getDurationDays() + "天，建议尽早就诊");
            } else if (request.getDurationDays() >= 3) {
                actions.add("症状持续" + request.getDurationDays() + "天，如无好转请及时就诊");
            }
        }

        // 默认建议
        if (actions.isEmpty()) {
            actions.add("如症状加重请立即就诊");
            actions.add("建议到校医院相关科室完善检查");
        }

        return actions;
    }

    /**
     * 匹配结果辅助类
     */
    private static class MatchResult {
        String departmentId;
        String departmentName;
        int matchCount;
        Set<String> matchedKeywords;

        MatchResult(String departmentId, String departmentName) {
            this.departmentId = departmentId;
            this.departmentName = departmentName;
            this.matchCount = 0;
            this.matchedKeywords = new LinkedHashSet<>();
        }

        void addMatches(List<String> matches) {
            matchCount += matches.size();
            matchedKeywords.addAll(matches);
        }
    }
}
