package org.smartvert.smartvert.service.impl;

import lombok.RequiredArgsConstructor;
import org.smartvert.smartvert.exception.ResourceNotFoundException;
import org.smartvert.smartvert.model.dto.UserResponse;
import org.smartvert.smartvert.model.entity.AppUser;
import org.smartvert.smartvert.repository.AppUserRepository;
import org.smartvert.smartvert.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final AppUserRepository appUserRepository;

    @Override
    public UserResponse getProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new ResourceNotFoundException("User not found");
        }

        AppUser user = appUserRepository.findByEmail(auth.getName().toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getUserType(),
                user.getState(),
                user.getPhoneNumber(),
                user.isEmailVerified()
        );
    }
}
