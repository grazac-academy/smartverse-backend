package org.smartvert.smartvert.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "appliance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appliance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private ApplianceCategory category;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "default_wattage", nullable = false, precision = 10, scale = 2)
    private BigDecimal defaultWattage;

    @Column(name = "min_wattage", nullable = false, precision = 10, scale = 2)
    private BigDecimal minWattage;

    @Column(name = "max_wattage", nullable = false, precision = 10, scale = 2)
    private BigDecimal maxWattage;

    @Column(name = "default_voltage", nullable = false)
    private Integer defaultVoltage;

    @Column(name = "surge_applicable", nullable = false)
    private Boolean surgeApplicable;

    @Column(name = "surge_multiplier", nullable = false, precision = 4, scale = 2)
    private BigDecimal surgeMultiplier;

    @Column(name = "heavy_load", nullable = false)
    private Boolean heavyLoad;

    @Column(nullable = false)
    private Boolean active;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

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
