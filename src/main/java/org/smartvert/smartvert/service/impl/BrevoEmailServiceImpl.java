package org.smartvert.smartvert.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Primary
@ConditionalOnProperty(name = "app.mail.provider", havingValue = "brevo", matchIfMissing = true)
@Slf4j
public class BrevoEmailServiceImpl extends AbstractEmailService {

    @Value("${app.mail.brevo.url:${BREVO_API_URL:https://api.brevo.com/v3/smtp/email}}")
    private String apiUrl;

    @Value("${app.mail.brevo.api-key:${BREVO_API_KEY:}}")
    private String apiKey;

    @Value("${app.mail.from:${MAIL_FROM:noreply@smartvert.org}}")
    private String fromEmail;

    @Value("${app.mail.from-name:${MAIL_FROM_NAME:SmartVert}}")
    private String fromName;

    private final ObjectMapper objectMapper;

    public BrevoEmailServiceImpl(@Autowired(required = false) ObjectMapper objectMapper) {
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
    }

    @Override
    @Async
    public void sendVerificationEmail(String toEmail, String fullName, String otpCode) {
        String subject = "Verify your email address - SmartVert";
        String htmlContent = buildVerificationEmailTemplate(fullName, otpCode);
        sendBrevoEmail(toEmail, fullName, subject, htmlContent, "OTP: " + otpCode);
    }

    @Override
    @Async
    public void sendPasswordResetEmail(String toEmail, String fullName, String otpCode) {
        String subject = "Reset your password - SmartVert";
        String htmlContent = buildPasswordResetEmailTemplate(fullName, otpCode);
        sendBrevoEmail(toEmail, fullName, subject, htmlContent, "OTP: " + otpCode);
    }

    private void sendBrevoEmail(String toEmail, String recipientName, String subject, String htmlContent, String actionUrl) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Brevo API key not configured. Email to [{}], Subject: [{}], Action URL: [{}]",
                    toEmail, subject, actionUrl);
            return;
        }

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("htmlContent", htmlContent);
            payload.put("subject", subject);
            payload.put("sender", Map.of(
                    "email", fromEmail,
                    "name", fromName
            ));
            payload.put("to", List.of(Map.of(
                    "email", toEmail,
                    "name", (recipientName != null && !recipientName.isBlank()) ? recipientName : toEmail
            )));

            String jsonBody = objectMapper.writeValueAsString(payload);

            HttpResponse<String> response = Unirest.post(apiUrl)
                    .header("api-key", apiKey)
                    .header("Content-Type", "application/json")
                    .body(jsonBody)
                    .asString();

            if (response.getStatus() >= 200 && response.getStatus() < 300) {
                log.info("Successfully delivered email to [{}] via Brevo API with subject [{}]. Response: {}",
                        toEmail, subject, response.getBody());
            } else {
                log.error("Failed to deliver email to [{}] via Brevo API. HTTP {}: {}. Action URL: [{}]",
                        toEmail, response.getStatus(), response.getBody(), actionUrl);
            }
        } catch (Exception e) {
            log.error("Error occurred while sending email to [{}] via Brevo API: {}. Action URL: [{}]",
                    toEmail, e.getMessage(), actionUrl, e);
        }
    }
}
