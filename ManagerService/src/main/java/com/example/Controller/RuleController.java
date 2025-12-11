package com.example.Controller;

import com.example.Conmon.result.Result;
import com.example.Service.RuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 规则控制器
 */
@RestController
@RequestMapping("/admin")
public class RuleController {

    @Autowired
    private RuleService ruleService;

    /**
     * 查看挂号规则
     * @return 挂号规则信息
     */
    @GetMapping("/getRule")
    public Result<Map<String, Object>> getRule() {
        return ruleService.getRegisterRule();
    }
    
    /**
     * 更新挂号规则
     * @param ruleMap 新的规则信息
     * @return 更新结果
     */
    @PutMapping("/setRule")
    public Result<String> setRule(@RequestBody Map<String, Object> ruleMap) {
        return ruleService.updateRegisterRule(ruleMap);
    }
}