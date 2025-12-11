package com.example.Service;

import com.example.Conmon.result.Result;

import java.util.Map;

/**
 * 规则服务接口
 */
public interface RuleService {
    /**
     * 获取挂号规则
     * @return 挂号规则信息
     */
    Result<Map<String, Object>> getRegisterRule();
    
    /**
     * 更新挂号规则
     * @param ruleMap 新的规则信息
     * @return 更新结果
     */
    Result<String> updateRegisterRule(Map<String, Object> ruleMap);
}