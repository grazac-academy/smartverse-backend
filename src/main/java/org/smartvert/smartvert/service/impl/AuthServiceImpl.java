package org.smartvert.smartvert.service.impl;

import lombok.RequiredArgsConstructor;
import org.smartvert.smartvert.exception.DuplicateResourceException;
import org.smartvert.smartvert.exception.ResourceNotFoundException;
import org.smartvert.smartvert.exception.ValidationException;
import org.smartvert.smartvert.model.dto.*;
import org.smartvert.smartvert.model.entity.AppUser;
import org.smartvert.smartvert.model.entity.UserToken;
import org.smartvert.smartvert.repository.AppUserRepository;
import org.smartvert.smartvert.repository.UserTokenRepository;
import org.smartvert.smartvert.security.JwtTokenProvider;
import org.smartvert.smartvert.service.AuthService;
import org.smartvert.smartvert.service.EmailService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final AppUserRepository appUserRepository;
    private final UserTokenRepository userTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (appUserRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("An account with this email already exists");
        }

        AppUser user = AppUser.builder()
                .email(request.email().toLowerCase().trim())
                .password(passwordEncoder.encode(request.password()))
                .fullName(request.fullName().trim())
                .userType(request.userType() != null && !request.userType().isBlank() ? request.userType().trim() : null)
                .state(request.state() != null && !request.state().isBlank() ? request.state().trim() : null)
                .phoneNumber(request.phoneNumber() != null && !request.phoneNumber().isBlank() ? request.phoneNumber().trim() : null)
                .isEmailVerified(false)
                .build();

        AppUser saved = appUserRepository.save(user);

        String otp = generateUniqueOtp();
        UserToken otpToken = UserToken.builder()
                .user(saved)
                .token(otp)
                .tokenType(UserToken.TokenType.EMAIL_VERIFICATION)
                .expiresAt(OffsetDateTime.now().plusHours(24))
                .build();
        userTokenRepository.save(otpToken);

        emailService.sendVerificationEmail(
                saved.getEmail(),
                saved.getFullName(),
                otp
        );
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email().toLowerCase().trim(),
                        request.password()
                )
        );

        AppUser user = appUserRepository.findByEmail(request.email().toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        var tokenPair = jwtTokenProvider.generateTokenPair(auth);

        return new AuthResponse(
                tokenPair.accessToken(),
                tokenPair.refreshToken(),
                user.isEmailVerified()
        );
    }

    @Override
    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {
        AppUser user = appUserRepository.findByEmail(request.email().toLowerCase().trim())
                .orElseThrow(() -> new ValidationException("Invalid or expired verification code"));

        UserToken userToken = userTokenRepository.findByUserAndTokenAndTokenType(user, request.otp().trim(), UserToken.TokenType.EMAIL_VERIFICATION)
                .orElseThrow(() -> new ValidationException("Invalid or expired verification code"));

        if (userToken.isUsed() || userToken.isExpired()) {
            throw new ValidationException("Invalid or expired verification code");
        }

        userToken.setUsedAt(OffsetDateTime.now());
        userTokenRepository.save(userToken);

        user.setEmailVerified(true);
        appUserRepository.save(user);
    }

    @Override
    @Transactional
    public void resendVerificationEmail(String email) {
        AppUser user = appUserRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.isEmailVerified()) {
            throw new ValidationException("Email is already verified");
        }

        String otp = generateUniqueOtp();
        UserToken otpToken = UserToken.builder()
                .user(user)
                .token(otp)
                .tokenType(UserToken.TokenType.EMAIL_VERIFICATION)
                .expiresAt(OffsetDateTime.now().plusHours(24))
                .build();
        userTokenRepository.save(otpToken);

        emailService.sendVerificationEmail(
                user.getEmail(),
                user.getFullName(),
                otp
        );
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {
        appUserRepository.findByEmail(email.toLowerCase().trim()).ifPresent(user -> {
            String otp = generateUniqueOtp();
            UserToken otpToken = UserToken.builder()
                    .user(user)
                    .token(otp)
                    .tokenType(UserToken.TokenType.PASSWORD_RESET)
                    .expiresAt(OffsetDateTime.now().plusHours(1))
                    .build();
            userTokenRepository.save(otpToken);

            emailService.sendPasswordResetEmail(
                    user.getEmail(),
                    user.getFullName(),
                    otp
            );
        });
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        AppUser user = appUserRepository.findByEmail(request.email().toLowerCase().trim())
                .orElseThrow(() -> new ValidationException("Invalid or expired password reset code"));

        UserToken userToken = userTokenRepository.findByUserAndTokenAndTokenType(user, request.otp().trim(), UserToken.TokenType.PASSWORD_RESET)
                .orElseThrow(() -> new ValidationException("Invalid or expired password reset code"));

        if (userToken.isUsed() || userToken.isExpired()) {
            throw new ValidationException("Invalid or expired password reset code");
        }

        userToken.setUsedAt(OffsetDateTime.now());
        userTokenRepository.save(userToken);

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        appUserRepository.save(user);
    }

    private String generateUniqueOtp() {
        java.security.SecureRandom random = new java.security.SecureRandom();
        String otp;
        do {
            otp = String.format("%06d", random.nextInt(1_000_000));
        } while (userTokenRepository.findByToken(otp).isPresent());
        return otp;
    }
}
