package com.example.Service;

import com.example.Mapper.RegistrationMapper;
import com.example.Mapper.WaitingMapper;
import com.example.pojo.dto.RegistrationDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 挂号规则测试
 */
@SpringBootTest
@Transactional
@Rollback
public class RegistrationRulesTest {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private RegistrationMapper registrationMapper;

    @Test
    @DisplayName("测试1: 有号源时可以正常挂号")
    public void testCreateRegistration_WithAvailableSource() {
        // 准备：找一个有号源的排班
        String patientId = "PAT0001";
        String scheduleId = "SCH001"; // 需要确保数据库有这条记录且有号源

        // 执行
        RegistrationDto result = registrationService.createRegistration(patientId, scheduleId);

        // 验证
        assertNotNull(result);
        assertTrue(result.isStatus(), "挂号应该成功");
        assertNotNull(result.getPaymentId(), "应该自动创建支付订单");
        assertNotNull(result.getAmount(), "应该有支付金额");
        
        System.out.println("✅ 测试通过：有号源时可以正常挂号");
        System.out.println("   - 支付ID: " + result.getPaymentId());
        System.out.println("   - 支付金额: " + result.getAmount());
    }

    @Test
    @DisplayName("测试2: 号源为0时挂号失败")
    public void testCreateRegistration_NoSource() {
        // 准备：找一个号源为0的排班
        String patientId = "PAT0002";
        String scheduleId = "SCH_NO_SOURCE"; // 需要数据库中有号源为0的排班

        // 执行
        RegistrationDto result = registrationService.createRegistration(patientId, scheduleId);

        // 验证
        assertNotNull(result);
        assertFalse(result.isStatus(), "号源为0时挂号应该失败");
        
        System.out.println("✅ 测试通过：号源为0时挂号失败");
    }

    @Test
    @DisplayName("测试3: 重复挂号应该失败")
    public void testCreateRegistration_Duplicate() {
        String patientId = "PAT0004";
        String scheduleId = "SCH002";

        // 第一次挂号
        RegistrationDto first = registrationService.createRegistration(patientId, scheduleId);
        assertTrue(first.isStatus(), "第一次挂号应该成功");

        // 第二次挂号应该抛异常
        assertThrows(Exception.class, () -> {
            registrationService.createRegistration(patientId, scheduleId);
        }, "重复挂号应该抛出异常");
        
        System.out.println("✅ 测试通过：重复挂号被阻止");
    }

    @Test
    @DisplayName("测试4: 取消挂号后号源回补")
    public void testCancelRegistration_SourceRestore() {
        String patientId = "PAT0005";
        String scheduleId = "SCH003";

        // 先挂号
        RegistrationDto reg = registrationService.createRegistration(patientId, scheduleId);
        assertTrue(reg.isStatus());

        // 记录取消前的号源
        Integer sourceBefore = registrationMapper.findScheduleLeftSource(scheduleId);

        // 取消挂号
        RegistrationDto cancelled = registrationService.cancelRegistration(patientId, scheduleId);
        assertNotNull(cancelled);

        // 验证号源已回补（应该+1）
        Integer sourceAfter = registrationMapper.findScheduleLeftSource(scheduleId);
        assertEquals(sourceBefore + 1, sourceAfter, "取消挂号后号源应该回补");
        
        System.out.println("✅ 测试通过：取消挂号后号源已回补");
        System.out.println("   - 取消前号源: " + sourceBefore);
        System.out.println("   - 取消后号源: " + sourceAfter);
    }

    @Test
    @DisplayName("测试5: 挂号成功后自动创建订单")
    public void testCreateRegistration_AutoCreateOrder() {
        String patientId = "PAT0007";
        String scheduleId = "SCH004";

        RegistrationDto result = registrationService.createRegistration(patientId, scheduleId);

        assertTrue(result.isStatus(), "挂号应该成功");
        assertNotNull(result.getPaymentId(), "应该自动创建支付订单");
        assertNotNull(result.getAmount(), "订单应该有金额");
        assertTrue(result.getAmount().compareTo(java.math.BigDecimal.ZERO) > 0, "金额应该大于0");
        
        System.out.println("✅ 测试通过：挂号成功自动创建订单");
        System.out.println("   - 订单ID: " + result.getPaymentId());
        System.out.println("   - 订单金额: " + result.getAmount());
    }
}
