package org.smartvert.smartvert.mapper;

import org.smartvert.smartvert.model.dto.ApplianceDTO;
import org.smartvert.smartvert.model.dto.ApplianceCategoryDTO;
import org.smartvert.smartvert.model.entity.Appliance;
import org.smartvert.smartvert.model.entity.ApplianceCategory;
import org.springframework.stereotype.Component;

@Component
public class ApplianceMapper {

    public ApplianceDTO toDTO(Appliance appliance) {
        if (appliance == null) {
            return null;
        }
        return new ApplianceDTO(
                appliance.getId(),
                appliance.getCategory().getId(),
                appliance.getCode(),
                appliance.getName(),
                appliance.getDefaultWattage(),
                appliance.getMinWattage(),
                appliance.getMaxWattage(),
                appliance.getDefaultVoltage(),
                appliance.getSurgeApplicable(),
                appliance.getSurgeMultiplier(),
                appliance.getHeavyLoad(),
                appliance.getActive(),
                appliance.getImageUrl()
        );
    }

    public ApplianceCategoryDTO toDTO(ApplianceCategory category) {
        if (category == null) {
            return null;
        }
        return new ApplianceCategoryDTO(
                category.getId(),
                category.getCode(),
                category.getName(),
                category.getDisplayOrder()
        );
    }
}
