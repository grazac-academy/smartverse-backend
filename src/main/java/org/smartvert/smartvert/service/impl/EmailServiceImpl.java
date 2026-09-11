package org.smartvert.smartvert.service.impl;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smartvert.smartvert.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.mail.from:${spring.mail.username:noreply@smartvert.org}}")
    private String fromEmail;

    @Value("${app.mail.from-name:SmartVert}")
    private String fromName;

    @Override
    @Async
    public void sendVerificationEmail(String toEmail, String fullName, String otpCode, String verificationUrl) {
        String subject = "Verify your email address - SmartVert";
        String htmlContent = buildVerificationEmailTemplate(fullName, otpCode, verificationUrl);
        sendHtmlEmail(toEmail, subject, htmlContent, verificationUrl);
    }

    @Override
    @Async
    public void sendPasswordResetEmail(String toEmail, String fullName, String otpCode, String resetUrl) {
        String subject = "Reset your password - SmartVert";
        String htmlContent = buildPasswordResetEmailTemplate(fullName, otpCode, resetUrl);
        sendHtmlEmail(toEmail, subject, htmlContent, resetUrl);
    }

    private void sendHtmlEmail(String toEmail, String subject, String htmlContent, String actionUrl) {
        if (mailSender == null) {
            log.info("JavaMailSender not configured. Email to [{}], Subject: [{}], Action URL: [{}]",
                    toEmail, subject, actionUrl);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Successfully sent email to [{}] with subject [{}]", toEmail, subject);
        } catch (Exception e) {
            log.warn("Failed to deliver email to [{}] via SMTP: {}. Link: [{}]", toEmail, e.getMessage(), actionUrl);
        }
    }

    private String buildVerificationEmailTemplate(String fullName, String otpCode, String verificationUrl) {
        String template = loadTemplate("email-verification.html");
        return template.replace("{{fullName}}", escapeHtml(fullName))
                .replace("{{otpCode}}", escapeHtml(otpCode))
                .replace("{{verificationUrl}}", verificationUrl);
    }

    private String buildPasswordResetEmailTemplate(String fullName, String otpCode, String resetUrl) {
        String template = loadTemplate("password-reset.html");
        return template.replace("{{fullName}}", escapeHtml(fullName))
                .replace("{{otpCode}}", escapeHtml(otpCode))
                .replace("{{resetUrl}}", resetUrl);
    }

    private String loadTemplate(String templateName) {
        try (InputStream is = getClass().getResourceAsStream("/templates/" + templateName)) {
            if (is == null) {
                log.warn("Template /templates/{} not found on classpath", templateName);
                return "";
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Error reading email template /templates/{}", templateName, e);
            return "";
        }
    }

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
