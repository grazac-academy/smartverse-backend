package org.smartvert.smartvert.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.smartvert.smartvert.model.entity.Appliance;
import org.smartvert.smartvert.model.entity.ApplianceCategory;
import org.smartvert.smartvert.model.entity.CalculationConfiguration;
import org.smartvert.smartvert.model.entity.InverterOption;
import org.smartvert.smartvert.repository.ApplianceCategoryRepository;
import org.smartvert.smartvert.repository.ApplianceRepository;
import org.smartvert.smartvert.repository.CalculationConfigurationRepository;
import org.smartvert.smartvert.repository.InverterOptionRepository;
import org.smartvert.smartvert.repository.CalculationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplianceCategoryRepository categoryRepository;

    @Autowired
    private ApplianceRepository applianceRepository;

    @Autowired
    private CalculationConfigurationRepository configRepository;

    @Autowired
    private InverterOptionRepository inverterOptionRepository;

    @Autowired
    private CalculationRepository calculationRepository;

    private UUID categoryId;
    private UUID applianceId;

    @BeforeEach
    void setUp() {
        calculationRepository.deleteAll();
        applianceRepository.deleteAll();
        categoryRepository.deleteAll();
        configRepository.deleteAll();
        inverterOptionRepository.deleteAll();

        ApplianceCategory category = categoryRepository.save(ApplianceCategory.builder()
                .code("kitchen")
                .name("Kitchen Appliances")
                .displayOrder(1)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build());
        categoryId = category.getId();

        Appliance appliance = applianceRepository.save(Appliance.builder()
                .category(category)
                .code("fridge")
                .name("Refrigerator")
                .defaultWattage(new BigDecimal("300.00"))
                .minWattage(new BigDecimal("100.00"))
                .maxWattage(new BigDecimal("800.00"))
                .defaultVoltage(220)
                .surgeApplicable(true)
                .surgeMultiplier(new BigDecimal("3.00"))
                .heavyLoad(false)
                .active(true)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build());
        applianceId = appliance.getId();

        configRepository.save(CalculationConfiguration.builder()
                .name("Default Configuration")
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
                .systemVoltage(12)
                .active(true)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build());
    }

    @Test
    void shouldGetCategories() throws Exception {
        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Categories retrieved successfully")))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].code", is("kitchen")))
                .andExpect(jsonPath("$.data[0].name", is("Kitchen Appliances")));
    }

    @Test
    void shouldGetAppliances() throws Exception {
        mockMvc.perform(get("/api/v1/appliances"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Appliances retrieved successfully")))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].code", is("fridge")))
                .andExpect(jsonPath("$.data[0].name", is("Refrigerator")));
    }

    @Test
    void shouldCalculateSizing() throws Exception {
        String requestBody = """
                {
                    "usageMode": "BACKUP",
                    "backupHours": 6.00,
                    "items": [
                        {
                            "applianceId": "%s",
                            "quantity": 1,
                            "wattage": 300.00,
                            "hoursPerDay": 8.00
                        }
                    ]
                }
                """.formatted(applianceId);

        mockMvc.perform(post("/api/v1/calculations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Calculation completed successfully")))
                .andExpect(jsonPath("$.data.summary.totalRunningLoadWatts", is(300.00)))
                .andExpect(jsonPath("$.data.recommendation.inverterKva", is(1.50)))
                .andExpect(jsonPath("$.data.recommendation.battery.capacityAh", is(245.10)));
    }

    @Test
    void shouldBlockAdminWithoutAuth() throws Exception {
        String requestBody = """
                {
                    "categoryId": "%s",
                    "code": "tv",
                    "name": "Television",
                    "defaultWattage": 100.00,
                    "minWattage": 50.00,
                    "maxWattage": 300.00,
                    "defaultVoltage": 220,
                    "surgeApplicable": false,
                    "surgeMultiplier": 1.00,
                    "heavyLoad": false,
                    "active": true
                }
                """.formatted(categoryId);

        mockMvc.perform(post("/api/v1/admin/appliances")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowAdminWithAuth() throws Exception {
        String requestBody = """
                {
                    "categoryId": "%s",
                    "code": "tv",
                    "name": "Television",
                    "defaultWattage": 100.00,
                    "minWattage": 50.00,
                    "maxWattage": 300.00,
                    "defaultVoltage": 220,
                    "surgeApplicable": false,
                    "surgeMultiplier": 1.00,
                    "heavyLoad": false,
                    "active": true
                }
                """.formatted(categoryId);

        mockMvc.perform(post("/api/v1/admin/appliances")
                .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Appliance created successfully")))
                .andExpect(jsonPath("$.data.code", is("tv")))
                .andExpect(jsonPath("$.data.name", is("Television")));
    }

    @Test
    void shouldFailCalculationWhenQuantityTooLarge() throws Exception {
        String requestBody = """
                {
                    "usageMode": "BACKUP",
                    "backupHours": 6.00,
                    "items": [
                        {
                            "applianceId": "%s",
                            "quantity": 1001,
                            "wattage": 300.00,
                            "hoursPerDay": 8.00
                        }
                    ]
                }
                """.formatted(applianceId);

        mockMvc.perform(post("/api/v1/calculations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Quantity cannot exceed 1000")));
    }

    @Test
    void shouldFailCalculationWhenCumulativeLoadExceedsDatabaseLimit() throws Exception {
        ApplianceCategory category = categoryRepository.findAll().get(0);
        Appliance heavyAppliance = applianceRepository.save(Appliance.builder()
                .category(category)
                .code("heavy-industrial")
                .name("Heavy Industrial Load")
                .defaultWattage(new BigDecimal("50000.00"))
                .minWattage(new BigDecimal("50000.00"))
                .maxWattage(new BigDecimal("50000.00"))
                .defaultVoltage(380)
                .surgeApplicable(true)
                .surgeMultiplier(new BigDecimal("2.00"))
                .heavyLoad(true)
                .active(true)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build());

        String requestBody = """
                {
                    "usageMode": "BACKUP",
                    "backupHours": 6.00,
                    "items": [
                        {
                            "applianceId": "%s",
                            "quantity": 1000,
                            "wattage": 50000.00,
                            "hoursPerDay": 24.00
                        }
                    ]
                }
                """.formatted(heavyAppliance.getId());

        mockMvc.perform(post("/api/v1/calculations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("exceeds maximum supported limit")));
    }
}
