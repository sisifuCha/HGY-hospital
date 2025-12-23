package com.example.Service.Impl;

import com.example.Conmon.result.Result;
import com.example.Mapper.WaitingRuleMapper;
import com.example.Service.WaitingRuleService;
import com.example.pojo.entity.WaitingRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 候补规则服务实现类
 */
@Service
public class WaitingRuleServiceImpl implements WaitingRuleService {

    @Autowired
    private WaitingRuleMapper waitingRuleMapper;

    @Override
    public Result<Map<String, Object>> getWaitingRules() {
        try {
            List<WaitingRule> rules = waitingRuleMapper.getAllWaitingRules();
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("rules", rules);
            return Result.success(resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail(500, "获取候补规则失败");
        }
    }

    @Override
    @Transactional
    public Result<String> changeRuleValue(Map<String, Integer> newValues) {
        try {
            // 验证参数是否为空
            if (newValues == null || newValues.isEmpty()) {
                return Result.fail(400, "没有需要更新的规则值");
            }

            // 批量更新规则值
            for (Map.Entry<String, Integer> entry : newValues.entrySet()) {
                Integer id = Integer.parseInt(entry.getKey());
                Integer ruleValue = entry.getValue();
                waitingRuleMapper.updateRuleValueById(id, ruleValue);
            }

            return Result.success("更新规则值成功");
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return Result.fail(400, "规则ID格式错误");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail(500, "更新规则值失败");
        }
    }
}