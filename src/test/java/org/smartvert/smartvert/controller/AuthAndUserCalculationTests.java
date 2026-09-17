package org.smartvert.smartvert.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.smartvert.smartvert.model.entity.*;
import org.smartvert.smartvert.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthAndUserCalculationTests {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private AppUserRepository appUserRepository;

        @Autowired
        private SavedCalculationRepository savedCalculationRepository;

        @Autowired
        private CalculationRepository calculationRepository;

        @Autowired
        private CalculationConfigurationRepository configRepository;

        @Autowired
        private ApplianceCategoryRepository categoryRepository;

        @Autowired
        private ApplianceRepository applianceRepository;

        @Autowired
        private InverterOptionRepository inverterOptionRepository;

        @Autowired
        private UserTokenRepository userTokenRepository;

        private UUID calculationId;

        @BeforeEach
        void setUp() {
                userTokenRepository.deleteAll();
                savedCalculationRepository.deleteAll();
                calculationRepository.deleteAll();
                appUserRepository.deleteAll();
                applianceRepository.deleteAll();
                categoryRepository.deleteAll();
                configRepository.deleteAll();
                inverterOptionRepository.deleteAll();

                CalculationConfiguration config = configRepository.save(CalculationConfiguration.builder()
                                .name("Default Config")
                                .version("v1.0")
                                .inverterSafetyMargin(new BigDecimal("1.25"))
                                .powerFactor(new BigDecimal("0.80"))
                                .inverterEfficiency(new BigDecimal("0.90"))
                                .batteryEfficiency(new BigDecimal("0.85"))
                                .batteryDod(new BigDecimal("0.80"))
                                .peakSunHours(new BigDecimal("4.50"))
                                .solarEfficiency(new BigDecimal("0.80"))
                                .defaultPanelWattage(new BigDecimal("400.00"))
                                .active(true)
                                .createdAt(OffsetDateTime.now())
                                .updatedAt(OffsetDateTime.now())
                                .build());

                inverterOptionRepository.save(InverterOption.builder()
                                .ratingKva(new BigDecimal("1.50"))
                                .continuousWatts(new BigDecimal("1200.00"))
                                .systemVoltage(24)
                                .active(true)
                                .build());

                ApplianceCategory category = categoryRepository.save(ApplianceCategory.builder()
                                .code("COOLING")
                                .name("Cooling")
                                .displayOrder(1)
                                .build());

                Appliance fan = applianceRepository.save(Appliance.builder()
                                .category(category)
                                .code("FAN")
                                .name("Ceiling Fan")
                                .defaultWattage(new BigDecimal("75.00"))
                                .minWattage(new BigDecimal("50.00"))
                                .maxWattage(new BigDecimal("120.00"))
                                .defaultVoltage(230)
                                .surgeApplicable(true)
                                .surgeMultiplier(new BigDecimal("1.50"))
                                .heavyLoad(false)
                                .active(true)
                                .build());

                Calculation calc = calculationRepository.save(Calculation.builder()
                                .usageMode("BACKUP")
                                .backupHours(new BigDecimal("6.00"))
                                .totalRunningWatts(new BigDecimal("300.00"))
                                .peakSurgeWatts(new BigDecimal("900.00"))
                                .dailyEnergyWh(new BigDecimal("1800.00"))
                                .recommendedInverterKva(new BigDecimal("1.50"))
                                .recommendedBatteryKwh(new BigDecimal("3.00"))
                                .recommendedBatteryAh(new BigDecimal("250.00"))
                                .recommendedSolarKw(new BigDecimal("0.50"))
                                .panelCount(2)
                                .inputPayload("""
                                                {
                                                    "usageMode": "BACKUP",
                                                    "backupHours": 6.00,
                                                    "items": [
                                                        {
                                                            "applianceId": "%s",
                                                            "quantity": 2,
                                                            "wattage": 75.00,
                                                            "hoursPerDay": 8.00
                                                        }
                                                    ]
                                                }
                                                """.formatted(fan.getId()))
                                .configuration(config)
                                .createdAt(OffsetDateTime.now())
                                .build());
                calculationId = calc.getId();
        }

        @Test
        void shouldRegisterAndLoginSuccessfully() throws Exception {
                String registerJson = """
                                {
                                    "email": "john@example.com",
                                    "password": "Password123!",
                                    "fullName": "John Doe"
                                }
                                """;

                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(registerJson))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message", is("Registration successful")));

                String loginJson = """
                                {
                                    "email": "john@example.com",
                                    "password": "Password123!"
                                }
                                """;

                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginJson))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message", is("Login successful")))
                                .andExpect(jsonPath("$.data.email", is("john@example.com")))
                                .andExpect(jsonPath("$.data.accessToken", notNullValue()))
                                .andExpect(jsonPath("$.data.refreshToken", notNullValue()));
        }

        @Test
        void shouldRejectDuplicateRegistration() throws Exception {
                String registerJson = """
                                {
                                    "email": "duplicate@example.com",
                                    "password": "Password123!",
                                    "fullName": "Jane Doe"
                                }
                                """;

                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(registerJson))
                                .andExpect(status().isOk());

                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(registerJson))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.message", containsString("already exists")));
        }

        @Test
        void shouldFailLoginWithBadCredentials() throws Exception {
                String registerJson = """
                                {
                                    "email": "valid@example.com",
                                    "password": "Password123!",
                                    "fullName": "Valid User"
                                }
                                """;

                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(registerJson))
                                .andExpect(status().isOk());

                String badLoginJson = """
                                {
                                    "email": "valid@example.com",
                                    "password": "WrongPassword!"
                                }
                                """;

                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(badLoginJson))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldSaveAndManageUserCalculations() throws Exception {
                // 1. Register user
                String registerJson = """
                                {
                                    "email": "solar.user@example.com",
                                    "password": "SecurePassword123!",
                                    "fullName": "Solar User"
                                }
                                """;

                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(registerJson))
                                .andExpect(status().isOk());

                // 2. Login user to get JWT token
                String loginJson = """
                                {
                                    "email": "solar.user@example.com",
                                    "password": "SecurePassword123!"
                                }
                                """;

                MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginJson))
                                .andExpect(status().isOk())
                                .andReturn();

                JsonNode loginNode = objectMapper.readTree(loginResult.getResponse().getContentAsString());
                String accessToken = loginNode.get("data").get("accessToken").asText();
                String authHeader = "Bearer " + accessToken;

                // 2. Save calculation
                String saveJson = """
                                {
                                    "calculationId": "%s",
                                    "label": "My Home Setup"
                                }
                                """.formatted(calculationId);

                MvcResult saveResult = mockMvc.perform(post("/api/v1/user/calculations")
                                .header("Authorization", authHeader)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(saveJson))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message", is("Calculation saved to profile")))
                                .andExpect(jsonPath("$.data.label", is("My Home Setup")))
                                .andExpect(jsonPath("$.data.calculationId", is(calculationId.toString())))
                                .andReturn();

                JsonNode saveNode = objectMapper.readTree(saveResult.getResponse().getContentAsString());
                String savedId = saveNode.get("data").get("id").asText();

                // 2b. Attempt to save the same calculation again -> should return 409 Conflict
                mockMvc.perform(post("/api/v1/user/calculations")
                                .header("Authorization", authHeader)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(saveJson))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.message",
                                                is("This calculation is already saved to your profile")));

                // 3a. Get calculation by ID and verify details
                mockMvc.perform(get("/api/v1/user/calculations/" + savedId)
                                .header("Authorization", authHeader))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message", is("Saved calculation retrieved")))
                                .andExpect(jsonPath("$.data.id", is(savedId)))
                                .andExpect(jsonPath("$.data.calculationResult.recommendation.battery.systemVoltage",
                                                is(24)))
                                .andExpect(jsonPath("$.data.calculationResult.recommendation.battery.dodPercentage",
                                                is(80.0)))
                                .andExpect(jsonPath("$.data.calculationResult.recommendation.solar.panelWatts",
                                                is(400.0)))
                                .andExpect(jsonPath("$.data.calculationResult.breakdown", hasSize(1)))
                                .andExpect(jsonPath("$.data.calculationResult.breakdown[0].applianceName",
                                                is("Ceiling Fan")))
                                .andExpect(jsonPath("$.data.calculationResult.breakdown[0].quantity", is(2)))
                                .andExpect(jsonPath("$.data.calculationResult.breakdown[0].wattage", is(75.0)))
                                .andExpect(jsonPath("$.data.calculationResult.breakdown[0].runningWatts", is(150.0)))
                                .andExpect(jsonPath("$.data.calculationResult.breakdown[0].hoursPerDay", is(8.0)))
                                .andExpect(jsonPath("$.data.calculationResult.breakdown[0].dailyEnergyWh", is(1200.0)))
                                .andExpect(jsonPath("$.data.calculationResult.breakdown[0].surgeApplicable", is(true)))
                                .andExpect(jsonPath("$.data.calculationResult.breakdown[0].surgeWatts", is(112.5)));

                // 3b. List calculations
                mockMvc.perform(get("/api/v1/user/calculations")
                                .header("Authorization", authHeader))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data", hasSize(1)))
                                .andExpect(jsonPath("$.data[0].label", is("My Home Setup")))
                                .andExpect(jsonPath("$.data[0].calculationResult.recommendation.battery.systemVoltage",
                                                is(24)))
                                .andExpect(jsonPath("$.data[0].calculationResult.recommendation.battery.dodPercentage",
                                                is(80.0)))
                                .andExpect(jsonPath("$.data[0].calculationResult.recommendation.solar.panelWatts",
                                                is(400.0)))
                                .andExpect(jsonPath("$.data[0].calculationResult.breakdown", hasSize(1)));

                // 4. Update label
                String updateJson = """
                                {
                                    "label": "Updated Home Setup"
                                }
                                """;

                mockMvc.perform(patch("/api/v1/user/calculations/" + savedId)
                                .header("Authorization", authHeader)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message", is("Saved calculation updated")))
                                .andExpect(jsonPath("$.data.label", is("Updated Home Setup")));

                // 5. Delete calculation
                mockMvc.perform(delete("/api/v1/user/calculations/" + savedId)
                                .header("Authorization", authHeader))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message", is("Saved calculation deleted")));

                // 6. Verify list is empty
                mockMvc.perform(get("/api/v1/user/calculations")
                                .header("Authorization", authHeader))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data", hasSize(0)));
        }

        @Test
        void shouldVerifyEmailSuccessfully() throws Exception {
                // Register user
                String registerJson = """
                                {
                                    "email": "verify.me@example.com",
                                    "password": "Password123!",
                                    "fullName": "Verify Me"
                                }
                                """;

                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(registerJson))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message", is("Registration successful")));

                AppUser user = appUserRepository.findByEmail("verify.me@example.com").orElseThrow();
                UserToken token = userTokenRepository.findTopByUserAndTokenTypeOrderByCreatedAtDesc(
                                user, UserToken.TokenType.EMAIL_VERIFICATION).orElseThrow();

                // Verify with token via GET (browser click)
                mockMvc.perform(get("/api/v1/auth/verify-email")
                                .param("token", token.getToken()))
                                .andExpect(status().isOk())
                                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                                .andExpect(content().string(containsString("Email Verified Successfully")));

                AppUser updated = appUserRepository.findByEmail("verify.me@example.com").orElseThrow();
                org.junit.jupiter.api.Assertions.assertTrue(updated.isEmailVerified());

                // Verify token cannot be reused via POST (API client)
                mockMvc.perform(post("/api/v1/auth/verify-email")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"token\":\"" + token.getToken() + "\"}"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message", containsString("Invalid or expired")));

                // Verify invalid token via GET (browser click) returns clean error HTML
                mockMvc.perform(get("/api/v1/auth/verify-email")
                                .param("token", "non-existent-token"))
                                .andExpect(status().isBadRequest())
                                .andExpect(content().string(containsString("Verification Failed")));
        }

        @Test
        void shouldHandleForgotPasswordAndResetPassword() throws Exception {
                // 1. Register user
                String registerJson = """
                                {
                                    "email": "reset.user@example.com",
                                    "password": "OldPassword123!",
                                    "fullName": "Reset User"
                                }
                                """;

                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(registerJson))
                                .andExpect(status().isOk());

                // 2. Request forgot password
                String forgotJson = """
                                {
                                    "email": "reset.user@example.com"
                                }
                                """;

                mockMvc.perform(post("/api/v1/auth/forgot-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(forgotJson))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message",
                                                containsString("password reset instructions have been sent")));

                AppUser user = appUserRepository.findByEmail("reset.user@example.com").orElseThrow();
                UserToken resetToken = userTokenRepository.findTopByUserAndTokenTypeOrderByCreatedAtDesc(
                                user, UserToken.TokenType.PASSWORD_RESET).orElseThrow();

                // 3. Verify GET reset-password page loads for browser
                mockMvc.perform(get("/api/v1/auth/reset-password")
                                .param("token", resetToken.getToken()))
                                .andExpect(status().isOk())
                                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                                .andExpect(content().string(containsString("Reset Your Password")))
                                .andExpect(content().string(containsString(resetToken.getToken())));

                // 4. Reset password via POST
                String resetJson = """
                                {
                                    "token": "%s",
                                    "newPassword": "NewPassword123!"
                                }
                                """.formatted(resetToken.getToken());

                mockMvc.perform(post("/api/v1/auth/reset-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(resetJson))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message", is("Password reset successfully")));

                // 4. Old password should fail
                String oldLoginJson = """
                                {
                                    "email": "reset.user@example.com",
                                    "password": "OldPassword123!"
                                }
                                """;

                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(oldLoginJson))
                                .andExpect(status().isUnauthorized());

                // 5. New password should succeed
                String newLoginJson = """
                                {
                                    "email": "reset.user@example.com",
                                    "password": "NewPassword123!"
                                }
                                """;

                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(newLoginJson))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.accessToken", notNullValue()));
        }

        @Test
        void shouldVerifyAndResetPasswordWithSixDigitOtp() throws Exception {
                // 1. Register user
                String registerJson = """
                                {
                                    "email": "otp.user@example.com",
                                    "password": "OtpPassword123!",
                                    "fullName": "OTP User"
                                }
                                """;

                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(registerJson))
                                .andExpect(status().isOk());

                AppUser user = appUserRepository.findByEmail("otp.user@example.com").orElseThrow();

                // 2. Find 6-digit OTP token in DB
                UserToken otpToken = userTokenRepository.findAll().stream()
                                .filter(t -> t.getUser().getId().equals(user.getId())
                                                && t.getTokenType() == UserToken.TokenType.EMAIL_VERIFICATION
                                                && t.getToken().length() == 6)
                                .findFirst()
                                .orElseThrow();

                org.junit.jupiter.api.Assertions.assertEquals(6, otpToken.getToken().length());

                // 3. Verify email with 6-digit OTP via API
                mockMvc.perform(post("/api/v1/auth/verify-email")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"token\":\"" + otpToken.getToken() + "\"}"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message", is("Email verified successfully")));

                AppUser verifiedUser = appUserRepository.findByEmail("otp.user@example.com").orElseThrow();
                org.junit.jupiter.api.Assertions.assertTrue(verifiedUser.isEmailVerified());

                // 4. Request password reset
                mockMvc.perform(post("/api/v1/auth/forgot-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"email\":\"otp.user@example.com\"}"))
                                .andExpect(status().isOk());

                // 5. Find 6-digit reset OTP
                UserToken resetOtpToken = userTokenRepository.findAll().stream()
                                .filter(t -> t.getUser().getId().equals(user.getId())
                                                && t.getTokenType() == UserToken.TokenType.PASSWORD_RESET
                                                && t.getToken().length() == 6)
                                .findFirst()
                                .orElseThrow();

                // 6. Reset password using 6-digit OTP
                String resetJson = """
                                {
                                    "token": "%s",
                                    "newPassword": "BrandNewPassword123!"
                                }
                                """.formatted(resetOtpToken.getToken());

                mockMvc.perform(post("/api/v1/auth/reset-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(resetJson))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message", is("Password reset successfully")));

                // 7. Login with new password
                String loginJson = """
                                {
                                    "email": "otp.user@example.com",
                                    "password": "BrandNewPassword123!"
                                }
                                """;

                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginJson))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.accessToken", notNullValue()));
        }
}
