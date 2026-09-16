package org.smartvert.smartvert.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.smartvert.smartvert.service.EmailService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
public abstract class AbstractEmailService implements EmailService {

    protected String buildVerificationEmailTemplate(String fullName, String otpCode, String verificationUrl) {
        String template = loadTemplate("email-verification.html");
        return template.replace("{{fullName}}", escapeHtml(fullName))
                .replace("{{otpCode}}", escapeHtml(otpCode))
                .replace("{{verificationUrl}}", verificationUrl != null ? verificationUrl : "");
    }

    protected String buildPasswordResetEmailTemplate(String fullName, String otpCode, String resetUrl) {
        String template = loadTemplate("password-reset.html");
        return template.replace("{{fullName}}", escapeHtml(fullName))
                .replace("{{otpCode}}", escapeHtml(otpCode))
                .replace("{{resetUrl}}", resetUrl != null ? resetUrl : "");
    }

    protected String loadTemplate(String templateName) {
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

    protected String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
