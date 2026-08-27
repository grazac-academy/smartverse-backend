package org.smartvert.smartvert.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "calculation_configuration")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalculationConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 30)
    private String version;

    @Column(name = "inverter_safety_margin", nullable = false, precision = 5, scale = 4)
    private BigDecimal inverterSafetyMargin;

    @Column(name = "power_factor", nullable = false, precision = 4, scale = 3)
    private BigDecimal powerFactor;

    @Column(name = "battery_dod", nullable = false, precision = 4, scale = 3)
    private BigDecimal batteryDod;

    @Column(name = "battery_efficiency", nullable = false, precision = 4, scale = 3)
    private BigDecimal batteryEfficiency;

    @Column(name = "inverter_efficiency", nullable = false, precision = 4, scale = 3)
    private BigDecimal inverterEfficiency;

    @Column(name = "solar_efficiency", nullable = false, precision = 4, scale = 3)
    private BigDecimal solarEfficiency;

    @Column(name = "peak_sun_hours", nullable = false, precision = 4, scale = 2)
    private BigDecimal peakSunHours;

    @Column(name = "default_panel_wattage", nullable = false, precision = 10, scale = 2)
    private BigDecimal defaultPanelWattage;

    @Column(nullable = false)
    private Boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
