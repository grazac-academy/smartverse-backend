package org.smartvert.smartvert.service;

import org.smartvert.smartvert.model.dto.*;

public interface AuthService {

    void register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    void verifyEmail(VerifyEmailRequest request);

    void resendVerificationEmail(String email);

    void forgotPassword(String email);

    void resetPassword(ResetPasswordRequest request);
}
