package org.smartvert.smartvert.service.impl;

import lombok.RequiredArgsConstructor;
import org.smartvert.smartvert.exception.ResourceNotFoundException;
import org.smartvert.smartvert.model.dto.UpdateProfileRequest;
import org.smartvert.smartvert.model.dto.UserResponse;
import org.smartvert.smartvert.model.entity.AppUser;
import org.smartvert.smartvert.repository.AppUserRepository;
import org.smartvert.smartvert.repository.UserTokenRepository;
import org.smartvert.smartvert.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final AppUserRepository appUserRepository;
    private final UserTokenRepository userTokenRepository;

    @Override
    public UserResponse getProfile() {
        AppUser user = getCurrentAuthenticatedUser();
        return mapToUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(UpdateProfileRequest request) {
        AppUser user = getCurrentAuthenticatedUser();

        if (request.fullName() != null && !request.fullName().isBlank()) {
            user.setFullName(request.fullName().trim());
        }
        if (request.phoneNumber() != null) {
            user.setPhoneNumber(request.phoneNumber().trim());
        }

        AppUser updated = appUserRepository.save(user);
        return mapToUserResponse(updated);
    }

    private UserResponse mapToUserResponse(AppUser user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getUserType(),
                user.getState(),
                user.getPhoneNumber(),
                user.isEmailVerified(),
                user.getCreatedAt()
        );
    }

    @Override
    @Transactional
    public void deleteAccount() {
        AppUser user = getCurrentAuthenticatedUser();
        user.setDeleted(true);
        user.setDeletedAt(OffsetDateTime.now());
        appUserRepository.save(user);
        userTokenRepository.deleteByUser(user);
    }

    private AppUser getCurrentAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new ResourceNotFoundException("User not found");
        }

        return appUserRepository.findByEmailAndDeletedFalse(auth.getName().toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
