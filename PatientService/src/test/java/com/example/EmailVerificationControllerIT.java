package com.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EmailVerificationControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 测试环境不应触发真实 SMTP 发送。
     * 这里用 MockBean 强制替换 EmailSender，避免 Spy/真实 Bean 带来的不确定性。
     */
    @MockBean
    private com.example.Service.EmailSender emailSender;

    @Test
    void send_shouldReturn200_andNotRealSendInTestProfile() throws Exception {
        var body = objectMapper.writeValueAsString(new java.util.HashMap<String, Object>() {{
            put("email", "mockmvc_send@example.com");
            put("scene", "REGISTER");
        }});

        mockMvc.perform(post("/api/email-verification/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.email").value("mockmvc_send@example.com"))
                .andExpect(jsonPath("$.data.scene").value("REGISTER"));

        verify(emailSender, org.mockito.Mockito.never())
                .sendVerificationCode(anyString(), anyString(), anyString(), anyLong());
    }

    @Test
    void verify_shouldReturn400_whenBadFormat() throws Exception {
        var body = objectMapper.writeValueAsString(new java.util.HashMap<String, Object>() {{
            put("email", "mockmvc_verify@example.com");
            put("scene", "REGISTER");
            put("code", "12");
        }});

        mockMvc.perform(post("/api/email-verification/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }
}
