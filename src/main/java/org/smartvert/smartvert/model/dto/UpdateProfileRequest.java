package org.smartvert.smartvert.model.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(max = 150, message = "Full name must not exceed 150 characters") String fullName,

        @Size(max = 50, message = "Phone number must not exceed 50 characters") String phoneNumber) {
}
