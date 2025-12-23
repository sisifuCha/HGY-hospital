package com.example.Service;

import com.example.Conmon.result.Result;

import java.util.Map;

/**
 * 候补规则服务接口
 */
public interface WaitingRuleService {
    /**
     * 获取所有候补规则
     * @return 候补规则列表
     */
    Result<Map<String, Object>> getWaitingRules();

    /**
     * 更新候补规则值
     * @param newValues 新的规则值映射（键为规则ID，值为新规则值）
     * @return 更新结果
     */
    Result<String> changeRuleValue(Map<String, Integer> newValues);
}