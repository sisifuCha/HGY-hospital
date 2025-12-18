package com.example.Service;

import com.example.Mapper.WaitingMapper;
import com.example.pojo.dto.WaitingDto;
import com.example.pojo.entity.WaitingRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 候补规则测试
 */
@SpringBootTest
@Transactional
@Rollback
public class WaitingRulesTest {

    @Autowired
    private WaitingService waitingService;

    @Autowired
    private WaitingMapper waitingMapper;

    @Autowired
    private WaitingRuleService waitingRuleService;

    @Test
    @DisplayName("测试1: 候补规则 - 单个患者最多5个候补")
    public void testWaitingRule_MaxPatientWaiting() {
        String patientId = "PAT_WAIT_001";
        
        // 获取规则值
        Integer maxPatientWaiting = waitingMapper.getRuleValue("MAX_PATIENT_WAITING");
        assertNotNull(maxPatientWaiting);
        assertEquals(5, maxPatientWaiting, "患者最大候补数应该是5");
        
        System.out.println("✅ 测试通过：患者最大候补数规则 = " + maxPatientWaiting);
    }

    @Test
    @DisplayName("测试2: 候补规则 - 单个排班最多100人候补")
    public void testWaitingRule_MaxWaitingCount() {
        Integer maxWaitingCount = waitingMapper.getRuleValue("MAX_WAITING_COUNT");
        assertNotNull(maxWaitingCount);
        assertEquals(100, maxWaitingCount, "排班最大候补数应该是100");
        
        System.out.println("✅ 测试通过：排班最大候补数规则 = " + maxWaitingCount);
    }

    @Test
    @DisplayName("测试3: 候补规则 - 就诊前3小时停止候补")
    public void testWaitingRule_StopHoursBefore() {
        Integer stopHoursBefore = waitingMapper.getRuleValue("STOP_HOURS_BEFORE");
        assertNotNull(stopHoursBefore);
        assertEquals(3, stopHoursBefore, "应该在就诊前3小时停止候补");
        
        System.out.println("✅ 测试通过：停止候补时间规则 = " + stopHoursBefore + "小时");
    }

    @Test
    @DisplayName("测试4: 候补成功 - 写入数据库")
    public void testCreateWaiting_Success() {
        String patientId = "PAT0019";
        String scheduleId = "SCH_NO_SOURCE"; // 需要一个号源为0的排班

        // 执行候补
        WaitingDto result = waitingService.createWaiting(patientId, scheduleId);

        // 验证
        assertNotNull(result);
        assertEquals("候补中", result.getStatus());
        assertNotNull(result.getWaitingId());
        assertTrue(result.getPosition() > 0, "候补位置应该大于0");
        
        // 验证数据库记录
        WaitingRecord record = waitingMapper.getWaitingRecordByKey(patientId, scheduleId);
        assertNotNull(record, "数据库应该有候补记录");
        assertEquals("候补中", record.getStatus());
        
        System.out.println("✅ 测试通过：候补成功写入数据库");
        System.out.println("   - 候补ID: " + result.getWaitingId());
        System.out.println("   - 候补位置: " + result.getPosition());
    }

    @Test
    @DisplayName("测试5: 候补规则验证 - 号源不为0时不能候补")
    public void testValidateWaiting_HasSource() {
        String patientId = "PAT_WAIT_003";
        String scheduleId = "SCH_HAS_SOURCE"; // 有号源的排班

        // 应该抛出异常
        assertThrows(IllegalArgumentException.class, () -> {
            waitingRuleService.validateWaitingRequest(patientId, scheduleId);
        }, "有号源时不应该允许候补");
        
        System.out.println("✅ 测试通过：有号源时候补被阻止");
    }

    @Test
    @DisplayName("测试6: 候补查询 - 按排班查询")
    public void testGetWaitingListBySchedule() {
        String scheduleId = "SCH_NO_SOURCE";
        
        // 创建多个候补
        waitingService.createWaiting("PAT0019", scheduleId);
        waitingService.createWaiting("PAT0020", scheduleId);
        waitingService.createWaiting("PAT0021", scheduleId);

        // 查询
        List<WaitingDto> list = waitingService.getWaitingListBySchedule(scheduleId);

        // 验证
        assertNotNull(list);
        assertEquals(3, list.size(), "应该有3个候补记录");
        
        // 验证位置顺序
        for (int i = 0; i < list.size(); i++) {
            assertEquals(i + 1, list.get(i).getPosition(), "位置应该从1开始递增");
        }
        
        System.out.println("✅ 测试通过：候补列表查询成功");
        System.out.println("   - 候补人数: " + list.size());
    }

    @Test
    @DisplayName("测试7: 候补取消")
    public void testCancelWaiting() {
        String patientId = "PAT0022";
        String scheduleId = "SCH_NO_SOURCE";

        // 先候补
        WaitingDto waiting = waitingService.createWaiting(patientId, scheduleId);
        assertEquals("候补中", waiting.getStatus());

        // 取消候补
        WaitingDto cancelled = waitingService.cancelWaiting(patientId, scheduleId);
        
        // 验证
        assertNotNull(cancelled);
        assertEquals("已取消", cancelled.getStatus());
        
        // 验证数据库状态
        WaitingRecord record = waitingMapper.getWaitingRecordByKey(patientId, scheduleId);
        assertEquals("已取消", record.getStatus());
        
        System.out.println("✅ 测试通过：候补取消成功");
    }

    @Test
    @DisplayName("测试8: 候补位置计算")
    public void testWaitingPosition() {
        String scheduleId = "SCH_NO_SOURCE";

        // 创建多个候补
        WaitingDto w1 = waitingService.createWaiting("PAT0019", scheduleId);
        WaitingDto w2 = waitingService.createWaiting("PAT0020", scheduleId);
        WaitingDto w3 = waitingService.createWaiting("PAT0021", scheduleId);

        // 验证位置
        assertEquals(1, w1.getPosition(), "第一个候补位置应该是1");
        assertEquals(2, w2.getPosition(), "第二个候补位置应该是2");
        assertEquals(3, w3.getPosition(), "第三个候补位置应该是3");
        
        System.out.println("✅ 测试通过：候补位置计算正确");
    }
}
