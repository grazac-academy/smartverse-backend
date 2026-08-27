package org.smartvert.smartvert.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "calculation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Calculation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "config_id", nullable = false)
    private CalculationConfiguration configuration;

    @Column(name = "usage_mode", nullable = false, length = 30)
    private String usageMode;

    @Column(name = "backup_hours", precision = 4, scale = 2)
    private BigDecimal backupHours;

    @Column(name = "total_running_watts", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalRunningWatts;

    @Column(name = "peak_surge_watts", nullable = false, precision = 10, scale = 2)
    private BigDecimal peakSurgeWatts;

    @Column(name = "daily_energy_wh", nullable = false, precision = 12, scale = 2)
    private BigDecimal dailyEnergyWh;

    @Column(name = "recommended_inverter_kva", nullable = false, precision = 6, scale = 2)
    private BigDecimal recommendedInverterKva;

    @Column(name = "recommended_battery_ah", nullable = false, precision = 10, scale = 2)
    private BigDecimal recommendedBatteryAh;

    @Column(name = "recommended_battery_kwh", nullable = false, precision = 10, scale = 2)
    private BigDecimal recommendedBatteryKwh;

    @Column(name = "recommended_solar_kw", nullable = false, precision = 8, scale = 3)
    private BigDecimal recommendedSolarKw;

    @Column(name = "panel_count", nullable = false)
    private Integer panelCount;

    @Column(name = "input_payload", nullable = false, columnDefinition = "TEXT")
    private String inputPayload;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }
}
