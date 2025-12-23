package com.example.Mapper;

import com.example.pojo.entity.WaitingRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface WaitingRuleMapper {
    /**
     * 获取所有候补规则
     * @return 候补规则列表
     */
    List<WaitingRule> getAllWaitingRules();

    /**
     * 根据ID更新候补规则值
     * @param id 规则ID
     * @param ruleValue 新的规则值
     * @return 更新影响的行数
     */
    int updateRuleValueById(@Param("id") Integer id, @Param("ruleValue") Integer ruleValue);
}