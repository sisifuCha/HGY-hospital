package com.example.Service.Impl;

import com.example.Conmon.result.Result;
import com.example.Service.RuleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 规则服务实现类
 */
@Service
public class RuleServiceImpl implements RuleService {

    private static final String RULE_FILE_PATH = "e:\\java-project\\HGY-hospital\\register_rule.json";

    @Override
    public Result<Map<String, Object>> getRegisterRule() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            File file = new File(RULE_FILE_PATH);
            Map<String, Object> ruleMap = objectMapper.readValue(file, Map.class);
            return Result.success(ruleMap);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.fail(500, "读取规则文件失败");
        }
    }

    @Override
    public Result<String> updateRegisterRule(Map<String, Object> ruleMap) {
        try {
            // 验证传入的规则Map是否包含必要的字段
            if (!ruleMap.containsKey("register_rule") || !ruleMap.containsKey("refund_fee_rule")) {
                return Result.fail(400, "缺少必要的规则字段");
            }

            // 验证register_rule结构
            Map<String, Object> registerRule = (Map<String, Object>) ruleMap.get("register_rule");
            if (!registerRule.containsKey("extra_register_time") || !registerRule.containsKey("release_time")) {
                return Result.fail(400, "register_rule缺少必要字段");
            }

            List<Map<String, Object>> extraRegisterTimeList = (List<Map<String, Object>>) registerRule.get("extra_register_time");
            for (Map<String, Object> item : extraRegisterTimeList) {
                if (!item.containsKey("reason") || !item.containsKey("time")) {
                    return Result.fail(400, "extra_register_time项目缺少必要字段");
                }
            }

            // 验证refund_fee_rule结构
            Map<String, Object> refundFeeRule = (Map<String, Object>) ruleMap.get("refund_fee_rule");
            if (!refundFeeRule.containsKey("gradients")) {
                return Result.fail(400, "refund_fee_rule缺少必要字段gradients");
            }

            List<Map<String, Object>> gradientsList = (List<Map<String, Object>>) refundFeeRule.get("gradients");
            for (Map<String, Object> gradient : gradientsList) {
                if (!gradient.containsKey("name") || !gradient.containsKey("time_condition") || !gradient.containsKey("fee_ratio")) {
                    return Result.fail(400, "gradients项目缺少必要字段");
                }
            }

            // 写入文件
            ObjectMapper objectMapper = new ObjectMapper();
            File file = new File(RULE_FILE_PATH);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, ruleMap);

            return Result.success("更新规则成功");
        } catch (ClassCastException e) {
            e.printStackTrace();
            return Result.fail(400, "规则字段类型错误");
        } catch (IOException e) {
            e.printStackTrace();
            return Result.fail(500, "写入规则文件失败");
        }
    }
}