package org.smartvert.smartvert.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "inverter_option")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InverterOption {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "rating_kva", nullable = false, unique = true, precision = 6, scale = 2)
    private BigDecimal ratingKva;

    @Column(name = "continuous_watts", nullable = false, precision = 10, scale = 2)
    private BigDecimal continuousWatts;

    @Column(name = "system_voltage", nullable = false)
    private Integer systemVoltage;

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
