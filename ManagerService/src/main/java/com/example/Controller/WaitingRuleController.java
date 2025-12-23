package com.example.Controller;

import com.example.Conmon.result.Result;
import com.example.Service.WaitingRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 候补规则控制器
 */
@RestController
@RequestMapping("/admin")
public class WaitingRuleController {

    @Autowired
    private WaitingRuleService waitingRuleService;

    /**
     * 查看候补规则
     * @return 候补规则列表
     */
    @GetMapping("/getWaitingRules")
    public Result<Map<String, Object>> getWaitingRules() {
        return waitingRuleService.getWaitingRules();
    }

    /**
     * 更改挂号规则表的值
     * @param requestBody 请求体，包含newValues字段
     * @return 更新结果
     */
    @PutMapping("/changeRuleValue")
    public Result<String> changeRuleValue(@RequestBody Map<String, Map<String, Integer>> requestBody) {
        Map<String, Integer> newValues = requestBody.get("newValues");
        return waitingRuleService.changeRuleValue(newValues);
    }
}