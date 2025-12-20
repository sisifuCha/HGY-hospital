package com.example;

import com.example.pojo.vo.PatientDetailVo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 纯编译/模型层冒烟测试（不依赖 PostgreSQL/Redis）。
 *
 * 说明：你当前环境未启动 PostgreSQL/Redis，所以这里不做 Spring Boot 上下文启动，
 * 仅验证关键 VO/实体类能正常加载、字段存在满足接口文档。
 */
public class PatientServiceSmokeTest {

    @Test
    void patientDetailVo_shouldHavePhoneField() {
        PatientDetailVo vo = new PatientDetailVo();
        vo.setPhone("13800138000");
        assertEquals("13800138000", vo.getPhone());
    }
}

