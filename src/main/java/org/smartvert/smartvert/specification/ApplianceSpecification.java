package org.smartvert.smartvert.specification;

import jakarta.persistence.criteria.Predicate;
import org.smartvert.smartvert.model.dto.ApplianceFilter;
import org.smartvert.smartvert.model.entity.Appliance;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ApplianceSpecification {

    public static Specification<Appliance> filter(ApplianceFilter filter) {
        return (root, query, cb) -> {
            if (filter == null) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(filter.getName())) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + filter.getName().trim().toLowerCase() + "%"));
            }

            if (StringUtils.hasText(filter.getCode())) {
                predicates.add(cb.like(cb.lower(root.get("code")), "%" + filter.getCode().trim().toLowerCase() + "%"));
            }

            if (StringUtils.hasText(filter.getSearch())) {
                String searchPattern = "%" + filter.getSearch().trim().toLowerCase() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("name")), searchPattern);
                Predicate codeMatch = cb.like(cb.lower(root.get("code")), searchPattern);
                predicates.add(cb.or(nameMatch, codeMatch));
            }

            if (filter.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), filter.getCategoryId()));
            }

            if (filter.getMinWattage() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("defaultWattage"), filter.getMinWattage()));
            }

            if (filter.getMaxWattage() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("defaultWattage"), filter.getMaxWattage()));
            }

            if (filter.getDefaultVoltage() != null) {
                predicates.add(cb.equal(root.get("defaultVoltage"), filter.getDefaultVoltage()));
            }

            if (filter.getSurgeApplicable() != null) {
                predicates.add(cb.equal(root.get("surgeApplicable"), filter.getSurgeApplicable()));
            }

            if (filter.getHeavyLoad() != null) {
                predicates.add(cb.equal(root.get("heavyLoad"), filter.getHeavyLoad()));
            }

            if (filter.getActive() != null) {
                predicates.add(cb.equal(root.get("active"), filter.getActive()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
