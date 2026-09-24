package org.smartvert.smartvert.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplianceFilter {
    private String name;
    private String code;
    private String search;
    private UUID categoryId;
    private BigDecimal minWattage;
    private BigDecimal maxWattage;
    private Integer defaultVoltage;
    private Boolean surgeApplicable;
    private Boolean heavyLoad;
    private Boolean active;
}
