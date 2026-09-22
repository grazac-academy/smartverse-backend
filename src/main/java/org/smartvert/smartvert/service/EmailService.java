package org.smartvert.smartvert.service;

public interface EmailService {

    void sendVerificationEmail(String toEmail, String fullName, String otpCode);

    void sendPasswordResetEmail(String toEmail, String fullName, String otpCode);
}

