package org.smartvert.smartvert.service;

import org.smartvert.smartvert.model.dto.UpdateProfileRequest;
import org.smartvert.smartvert.model.dto.UserResponse;

public interface UserService {
    UserResponse getProfile();
    UserResponse updateProfile(UpdateProfileRequest request);
    void deleteAccount();
}

