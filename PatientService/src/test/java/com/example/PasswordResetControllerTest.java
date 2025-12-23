package com.example;

import com.example.Mapper.UserMapper;
import com.example.Service.EmailSender;
import com.example.utils.EmailVerificationCodeStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestMailConfig.class)
class PasswordResetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserMapper userMapper;

    @MockBean
    private EmailVerificationCodeStore codeStore;

    @MockBean
    private EmailSender emailSender;

    @BeforeEach
    void setup() {
        // 邮件发送不抛异常
        doNothing().when(emailSender).sendVerificationCode(anyString(), anyString(), anyString(), anyLong());
    }

    @Test
    void send_shouldReturn404_whenEmailNotRegistered() throws Exception {
        when(userMapper.findByEmail("nobody@example.com")).thenReturn(null);

        mockMvc.perform(post("/api/password-reset/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nobody@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void confirm_shouldReturn410_whenCodeMissing() throws Exception {
        var u = new com.example.pojo.entity.User();
        u.setUserId("PAT0001");
        u.setUserEmail("test@example.com");
        when(userMapper.findByEmail("test@example.com")).thenReturn(u);
        when(codeStore.getCode(anyString())).thenReturn(null);

        mockMvc.perform(post("/api/password-reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\",\"code\":\"123456\",\"newPassword\":\"a\",\"confirmPassword\":\"a\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(410))
                .andExpect(jsonPath("$.data.reset").value(false));
    }

    @Test
    void confirm_shouldResetPassword_whenCodeMatches() throws Exception {
        var u = new com.example.pojo.entity.User();
        u.setUserId("PAT0001");
        u.setUserEmail("test@example.com");
        when(userMapper.findByEmail("test@example.com")).thenReturn(u);
        when(codeStore.getCode(anyString())).thenReturn("123456");
        when(userMapper.updatePasswordByUserId("PAT0001", "newPass")).thenReturn(1);

        mockMvc.perform(post("/api/password-reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\",\"code\":\"123456\",\"newPassword\":\"newPass\",\"confirmPassword\":\"newPass\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.reset").value(true))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));
    }
}
