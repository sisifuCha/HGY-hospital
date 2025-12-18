package com.example.Service;

import com.example.Mapper.RegistrationMapper;
import com.example.Mapper.WaitingMapper;
import com.example.pojo.dto.RegistrationDto;
import com.example.pojo.dto.WaitingDto;
import com.example.pojo.entity.WaitingRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 候补转正规则测试
 */
@SpringBootTest
@Transactional
@Rollback
public class WaitingPromotionTest {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private WaitingService waitingService;

    @Autowired
    private WaitingMapper waitingMapper;

    @Autowired
    private RegistrationMapper registrationMapper;

    @Test
    @DisplayName("测试1: 取消挂号时自动转正候补患者")
    public void testCancelRegistration_PromoteWaiting() {
        String scheduleId = "SCH_PROMOTE_001";
        String patient1 = "PAT0008"; // 挂号患者
        String patient2 = "PAT0009"; // 候补患者

        // 步骤1：patient1 挂号
        RegistrationDto reg1 = registrationService.createRegistration(patient1, scheduleId);
        assertTrue(reg1.isStatus(), "挂号应该成功");

        // 步骤2：号源用完后 patient2 候补
        WaitingDto waiting = waitingService.createWaiting(patient2, scheduleId);
        assertEquals("候补中", waiting.getStatus());
        assertEquals(1, waiting.getPosition(), "应该是第一个候补");

        // 步骤3：patient1 取消挂号，应该自动转正 patient2
        registrationService.cancelRegistration(patient1, scheduleId);

        // 验证1：候补记录状态变为"已转正"
        WaitingRecord record = waitingMapper.getWaitingRecordByKey(patient2, scheduleId);
        assertEquals("已转正", record.getStatus(), "候补记录应该变为已转正");

        // 验证2：patient2 应该有挂号记录
        RegistrationDto reg2 = registrationService.getRegistrationByKey(patient2, scheduleId);
        assertNotNull(reg2, "转正后应该有挂号记录");
        // Note: status字段是boolean类型，不是字符串
        assertTrue(reg2.isStatus(), "转正后挂号应该成功");

        // 验证3：应该自动创建支付订单
        assertNotNull(reg2.getPaymentId(), "转正后应该自动创建订单");

        System.out.println("✅ 测试通过：取消挂号自动转正候补患者");
        System.out.println("   - 候补患者: " + patient2);
        System.out.println("   - 转正订单ID: " + reg2.getPaymentId());
    }

    @Test
    @DisplayName("测试2: 候补队列按时间顺序转正")
    public void testWaitingPromotion_FIFO() throws InterruptedException {
        String scheduleId = "SCH_PROMOTE_002";
        String patient1 = "PAT0010";
        String waitingPatient1 = "PAT0011";
        String waitingPatient2 = "PAT0012";
        String waitingPatient3 = "PAT0013";

        // 步骤1：patient1 挂号
        registrationService.createRegistration(patient1, scheduleId);

        // 步骤2：三个患者依次候补
        waitingService.createWaiting(waitingPatient1, scheduleId); // 第1个候补
        Thread.sleep(100); // 确保时间不同
        waitingService.createWaiting(waitingPatient2, scheduleId); // 第2个候补
        Thread.sleep(100);
        waitingService.createWaiting(waitingPatient3, scheduleId); // 第3个候补

        // 步骤3：取消挂号
        registrationService.cancelRegistration(patient1, scheduleId);

        // 验证：应该是第一个候补的患者转正
        WaitingRecord record1 = waitingMapper.getWaitingRecordByKey(waitingPatient1, scheduleId);
        assertEquals("已转正", record1.getStatus(), "第一个候补应该转正");

        WaitingRecord record2 = waitingMapper.getWaitingRecordByKey(waitingPatient2, scheduleId);
        assertEquals("候补中", record2.getStatus(), "第二个候补应该还在候补中");

        WaitingRecord record3 = waitingMapper.getWaitingRecordByKey(waitingPatient3, scheduleId);
        assertEquals("候补中", record3.getStatus(), "第三个候补应该还在候补中");

        System.out.println("✅ 测试通过：候补按FIFO顺序转正");
    }

    @Test
    @DisplayName("测试3: 无候补时取消挂号回补号源")
    public void testCancelRegistration_NoWaiting_RestoreSource() {
        String scheduleId = "SCH_PROMOTE_003";
        String patient1 = "PAT0014";

        // 步骤1：挂号
        registrationService.createRegistration(patient1, scheduleId);

        // 记录当前号源
        Integer sourceBefore = registrationMapper.findScheduleLeftSource(scheduleId);

        // 步骤2：取消挂号（没有候补患者）
        registrationService.cancelRegistration(patient1, scheduleId);

        // 验证：号源应该+1
        Integer sourceAfter = registrationMapper.findScheduleLeftSource(scheduleId);
        assertEquals(sourceBefore + 1, sourceAfter, "无候补时号源应该回补");

        System.out.println("✅ 测试通过：无候补时取消挂号号源回补");
        System.out.println("   - 取消前号源: " + sourceBefore);
        System.out.println("   - 取消后号源: " + sourceAfter);
    }

    @Test
    @DisplayName("测试4: 转正后自动创建订单")
    public void testWaitingPromotion_AutoCreateOrder() {
        String scheduleId = "SCH_PROMOTE_004";
        String patient1 = "PAT0015";
        String patient2 = "PAT0016";

        // 挂号 + 候补
        registrationService.createRegistration(patient1, scheduleId);
        waitingService.createWaiting(patient2, scheduleId);

        // 取消挂号触发转正
        registrationService.cancelRegistration(patient1, scheduleId);

        // 验证：转正患者应该有订单
        RegistrationDto promoted = registrationService.getRegistrationByKey(patient2, scheduleId);
        assertNotNull(promoted, "转正患者应该有挂号记录");
        assertNotNull(promoted.getPaymentId(), "转正后应该自动创建订单");
        assertNotNull(promoted.getAmount(), "订单应该有金额");

        System.out.println("✅ 测试通过：转正后自动创建订单");
        System.out.println("   - 订单ID: " + promoted.getPaymentId());
        System.out.println("   - 订单金额: " + promoted.getAmount());
    }

    @Test
    @DisplayName("测试5: 候补转正不回补号源")
    public void testWaitingPromotion_NoSourceRestore() {
        String scheduleId = "SCH_PROMOTE_005";
        String patient1 = "PAT0017";
        String patient2 = "PAT0018";

        // 挂号 + 候补
        registrationService.createRegistration(patient1, scheduleId);
        waitingService.createWaiting(patient2, scheduleId);

        // 记录号源
        Integer sourceBefore = registrationMapper.findScheduleLeftSource(scheduleId);

        // 取消挂号触发转正
        registrationService.cancelRegistration(patient1, scheduleId);

        // 验证：号源不变（因为给了候补患者）
        Integer sourceAfter = registrationMapper.findScheduleLeftSource(scheduleId);
        assertEquals(sourceBefore, sourceAfter, "转正时号源不应该回补（直接给候补患者）");

        System.out.println("✅ 测试通过：转正时号源不回补");
        System.out.println("   - 号源保持: " + sourceAfter);
    }
}
