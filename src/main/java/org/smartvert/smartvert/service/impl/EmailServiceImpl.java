package org.smartvert.smartvert.service.impl;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service("smtpEmailService")
@ConditionalOnProperty(name = "app.mail.provider", havingValue = "smtp")
@Slf4j
@RequiredArgsConstructor
public class EmailServiceImpl extends AbstractEmailService {

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
}

