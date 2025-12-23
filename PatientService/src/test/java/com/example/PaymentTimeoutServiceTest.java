package com.example;

import com.example.Mapper.PaymentMapper;
import com.example.Mapper.RegistrationMapper;
import com.example.Service.PaymentService;
import com.example.Service.PaymentTimeoutServiceImpl;
import com.example.pojo.dto.WaitingDto;
import com.example.pojo.entity.PayRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = com.example.PatientServiceApplication.class, properties = {
        "patient.payment.timeoutScanEnabled=true"
})
@ActiveProfiles("test")
@Import(TestMailConfig.class)
class PaymentTimeoutServiceTest {

    @Autowired
    private PaymentTimeoutServiceImpl paymentTimeoutService;

    @MockBean
    private PaymentMapper paymentMapper;

    @MockBean
    private RegistrationMapper registrationMapper;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private RedisTemplate<String, Object> redisTemplate;

    private ListOperations<String, Object> listOps;
    private SetOperations<String, Object> setOps;

    @BeforeEach
    void setupRedisMocks() {
        listOps = mock(ListOperations.class);
        setOps = mock(SetOperations.class);
        when(redisTemplate.opsForList()).thenReturn(listOps);
        when(redisTemplate.opsForSet()).thenReturn(setOps);
    }

    @Test
    void shouldCancelExpiredPayment_andPromoteWaiting_andCreateNewOrder() {
        PayRecord expired = new PayRecord();
        expired.setId("PAY1");
        expired.setPatientId("PAT_A");
        expired.setSchId("SCH1");

        when(paymentMapper.findExpiredUnpaidPayments(anyInt())).thenReturn(List.of(expired));
        when(paymentMapper.cancelPaymentIfUnpaid("PAY1")).thenReturn(1);

        WaitingDto w = new WaitingDto();
        w.setPatientId("PAT_B");
        w.setScheduleRecordId("SCH1");
        when(listOps.leftPop("waiting:queue:SCH1")).thenReturn(w);

        when(registrationMapper.insertRegistration("PAT_B", "SCH1", "待支付")).thenReturn(1);
        when(paymentService.createPayment("PAT_B", "SCH1")).thenReturn(null);

        int processed = paymentTimeoutService.processExpiredUnpaidPayments();

        // 1) 取消原订单
        verify(paymentMapper).cancelPaymentIfUnpaid("PAY1");
        // 2) 取消原挂号
        verify(registrationMapper).updateRegistrationStatusToCanceled("PAT_A", "SCH1");
        // 3) 候补转正插入挂号
        verify(registrationMapper).insertRegistration("PAT_B", "SCH1", "待支付");
        // 4) 候补转正生成订单
        verify(paymentService).createPayment("PAT_B", "SCH1");
        // 5) 清理候补 set
        verify(setOps).remove("waiting:set:SCH1", "PAT_B");
        verify(setOps).remove("waiting:patient_schedules:PAT_B", "SCH1");

        org.junit.jupiter.api.Assertions.assertEquals(1, processed);
    }

    @Test
    void shouldCancelExpiredPayment_andRestoreSource_whenNoWaiting() {
        PayRecord expired = new PayRecord();
        expired.setId("PAY2");
        expired.setPatientId("PAT_A");
        expired.setSchId("SCH2");

        when(paymentMapper.findExpiredUnpaidPayments(anyInt())).thenReturn(List.of(expired));
        when(paymentMapper.cancelPaymentIfUnpaid("PAY2")).thenReturn(1);
        when(listOps.leftPop("waiting:queue:SCH2")).thenReturn(null);

        int processed = paymentTimeoutService.processExpiredUnpaidPayments();

        verify(registrationMapper).incrementScheduleLeftSource("SCH2");
        org.junit.jupiter.api.Assertions.assertEquals(1, processed);
    }
}
