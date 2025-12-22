package com.example;

import com.example.Service.EmailVerificationService;
import com.example.Service.EmailSender;
import com.example.conmon.result.Result;
import com.example.pojo.dto.EmailVerificationSendRequest;
import com.example.pojo.dto.EmailVerificationVerifyRequest;
import com.example.pojo.dto.EmailVerificationSendResponse;
import com.example.utils.EmailVerificationCodeStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
class EmailVerificationServiceTest {

    @Autowired
    private EmailVerificationService emailVerificationService;

    @Autowired
    private EmailVerificationCodeStore codeStore;

    @MockBean
    private EmailSender emailSender;

    @Test
    void sendCode_shouldSucceed_andNotCallSmtp_whenRealSendDisabled() {
        EmailVerificationSendRequest req = new EmailVerificationSendRequest();
        req.setEmail("send_only@example.com");
        req.setScene("REGISTER");

        Result<?> r = emailVerificationService.sendCode(req);
        assertThat(r.getCode()).isEqualTo(200);

        // 默认配置 patient.emailVerification.realSendEnabled=false
        verify(emailSender, never()).sendVerificationCode(anyString(), anyString(), anyString(), anyLong());
    }

    @Test
    void verifyCode_shouldReturn400_whenCodeMismatch_and200_whenMatches() {
        // 先发一次，生成并保存验证码
        EmailVerificationSendRequest sendReq = new EmailVerificationSendRequest();
        sendReq.setEmail("verify_flow@example.com");
        sendReq.setScene("REGISTER");

        Result<EmailVerificationSendResponse> sendResult = emailVerificationService.sendCode(sendReq);
        assertThat(sendResult.getCode()).isEqualTo(200);

        // 取出存储的验证码（test profile 使用内存 store）
        String key = com.example.utils.EmailVerificationStore.buildKey("verify_flow@example.com", "REGISTER");
        String realCode = codeStore.getCode(key);
        assertThat(realCode).isNotBlank();

        // 1) 错误验证码 -> 400
        EmailVerificationVerifyRequest bad = new EmailVerificationVerifyRequest();
        bad.setEmail("verify_flow@example.com");
        bad.setScene("REGISTER");
        bad.setCode("000000");
        Result<?> badResult = emailVerificationService.verifyCode(bad);
        assertThat(badResult.getCode()).isEqualTo(400);

        // 2) 正确验证码 -> 200
        EmailVerificationVerifyRequest ok = new EmailVerificationVerifyRequest();
        ok.setEmail("verify_flow@example.com");
        ok.setScene("REGISTER");
        ok.setCode(realCode);
        Result<?> okResult = emailVerificationService.verifyCode(ok);
        assertThat(okResult.getCode()).isEqualTo(200);

        // 3) 再验证一次（已删除）-> 410
        Result<?> again = emailVerificationService.verifyCode(ok);
        assertThat(again.getCode()).isEqualTo(410);
    }
}
