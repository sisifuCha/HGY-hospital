package com.example;

import com.example.Mapper.PaymentMapper;
import com.example.pojo.dto.PaymentQuoteDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestMailConfig.class)
class PaymentQuoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentMapper paymentMapper;

    @Test
    void quote_shouldCalculateAmounts_andReturn200() throws Exception {
        PaymentQuoteDto db = new PaymentQuoteDto();
        db.setPatientId("PAT0001");
        db.setScheduleRecordId("SCH001");
        db.setOriAmount(new BigDecimal("100.00"));
        db.setReimburseType("职工医保");
        db.setReimbursePercent(new BigDecimal("30.00"));
        db.setMedicalInsuranceOverage(new BigDecimal("120.50"));

        when(paymentMapper.getPaymentQuote(anyString(), anyString())).thenReturn(db);

        mockMvc.perform(get("/api/payments/quote")
                        .param("patientId", "PAT0001")
                        .param("scheduleRecordId", "SCH001")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.oriAmount").value(100.00))
                .andExpect(jsonPath("$.data.reimbursePercent").value(30.00))
                .andExpect(jsonPath("$.data.askPayAmount").value(70.00))
                .andExpect(jsonPath("$.data.reimburseAmount").value(30.00))
                .andExpect(jsonPath("$.data.insuranceEnough").value(true));
    }

    @Test
    void quote_shouldReturn404_whenScheduleMissing() throws Exception {
        when(paymentMapper.getPaymentQuote(anyString(), anyString())).thenReturn(null);

        mockMvc.perform(get("/api/payments/quote")
                        .param("patientId", "PAT0001")
                        .param("scheduleRecordId", "SCH404")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void quote_shouldMarkNotEnough_whenOverageNull() throws Exception {
        PaymentQuoteDto db = new PaymentQuoteDto();
        db.setPatientId("PAT0001");
        db.setScheduleRecordId("SCH001");
        db.setOriAmount(new BigDecimal("50.00"));
        db.setReimbursePercent(new BigDecimal("0.00"));
        db.setMedicalInsuranceOverage(null);

        when(paymentMapper.getPaymentQuote(anyString(), anyString())).thenReturn(db);

        mockMvc.perform(get("/api/payments/quote")
                        .param("patientId", "PAT0001")
                        .param("scheduleRecordId", "SCH001")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.askPayAmount").value(50.00))
                .andExpect(jsonPath("$.data.insuranceEnough").value(false));
    }
}
