package org.smartvert.smartvert.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.smartvert.smartvert.service.impl.BrevoEmailServiceImpl;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class BrevoEmailServiceImplTest {

    private BrevoEmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        emailService = new BrevoEmailServiceImpl(new ObjectMapper());
        ReflectionTestUtils.setField(emailService, "apiUrl", "https://api.brevo.com/v3/smtp/email");
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@smartvert.org");
        ReflectionTestUtils.setField(emailService, "fromName", "SmartVert");
    }

    @Test
    void sendVerificationEmail_whenApiKeyIsBlank_doesNotThrow() {
        ReflectionTestUtils.setField(emailService, "apiKey", "");

        assertDoesNotThrow(() -> emailService.sendVerificationEmail(
                "user@example.com",
                "Jane Doe",
                "123456",
                "https://smartvert.org/verify?token=xyz"
        ));
    }

    @Test
    void sendPasswordResetEmail_whenApiKeyIsBlank_doesNotThrow() {
        ReflectionTestUtils.setField(emailService, "apiKey", "");

        assertDoesNotThrow(() -> emailService.sendPasswordResetEmail(
                "user@example.com",
                "Jane Doe",
                "654321",
                "https://smartvert.org/reset?token=xyz"
        ));
    }

    @Test
    void sendVerificationEmail_withDummyKey_handlesGracefully() {
        ReflectionTestUtils.setField(emailService, "apiKey", "dummy-key");

        assertDoesNotThrow(() -> emailService.sendVerificationEmail(
                "user@example.com",
                "Jane Doe",
                "123456",
                "https://smartvert.org/verify?token=xyz"
        ));
    }
}
