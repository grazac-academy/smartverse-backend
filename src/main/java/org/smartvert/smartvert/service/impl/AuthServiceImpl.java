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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final AppUserRepository appUserRepository;
    private final UserTokenRepository userTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

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

        String rawToken = UUID.randomUUID().toString();
        UserToken linkToken = UserToken.builder()
                .user(saved)
                .token(rawToken)
                .tokenType(UserToken.TokenType.EMAIL_VERIFICATION)
                .expiresAt(OffsetDateTime.now().plusHours(24))
                .build();
        userTokenRepository.save(linkToken);

        emailService.sendVerificationEmail(
                saved.getEmail(),
                saved.getFullName(),
                otp,
                baseUrl + "/api/v1/auth/verify-email?token=" + rawToken
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
                user.getEmail(),
                user.getFullName(),
                user.isEmailVerified()
        );
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        UserToken userToken = userTokenRepository.findByTokenAndTokenType(token.trim(), UserToken.TokenType.EMAIL_VERIFICATION)
                .orElseThrow(() -> new ValidationException("Invalid or expired verification code/link"));

        if (userToken.isUsed() || userToken.isExpired()) {
            throw new ValidationException("Invalid or expired verification code/link");
        }

        userToken.setUsedAt(OffsetDateTime.now());
        userTokenRepository.save(userToken);

        AppUser user = userToken.getUser();
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

        String rawToken = UUID.randomUUID().toString();
        UserToken linkToken = UserToken.builder()
                .user(user)
                .token(rawToken)
                .tokenType(UserToken.TokenType.EMAIL_VERIFICATION)
                .expiresAt(OffsetDateTime.now().plusHours(24))
                .build();
        userTokenRepository.save(linkToken);

        emailService.sendVerificationEmail(
                user.getEmail(),
                user.getFullName(),
                otp,
                baseUrl + "/api/v1/auth/verify-email?token=" + rawToken
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

            String rawToken = UUID.randomUUID().toString();
            UserToken linkToken = UserToken.builder()
                    .user(user)
                    .token(rawToken)
                    .tokenType(UserToken.TokenType.PASSWORD_RESET)
                    .expiresAt(OffsetDateTime.now().plusHours(1))
                    .build();
            userTokenRepository.save(linkToken);

            emailService.sendPasswordResetEmail(
                    user.getEmail(),
                    user.getFullName(),
                    otp,
                    baseUrl + "/api/v1/auth/reset-password?token=" + rawToken
            );
        });
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        UserToken userToken = userTokenRepository.findByTokenAndTokenType(token.trim(), UserToken.TokenType.PASSWORD_RESET)
                .orElseThrow(() -> new ValidationException("Invalid or expired password reset code/link"));

        if (userToken.isUsed() || userToken.isExpired()) {
            throw new ValidationException("Invalid or expired password reset code/link");
        }

        userToken.setUsedAt(OffsetDateTime.now());
        userTokenRepository.save(userToken);

        AppUser user = userToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
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
