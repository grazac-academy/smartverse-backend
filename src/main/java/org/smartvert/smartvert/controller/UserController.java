package org.smartvert.smartvert.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.smartvert.smartvert.model.dto.ApiResponse;
import org.smartvert.smartvert.model.dto.UpdateProfileRequest;
import org.smartvert.smartvert.model.dto.UserResponse;
import org.smartvert.smartvert.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile() {
        UserResponse response = userService.getProfile();
        return ResponseEntity.ok(ApiResponse.success("User profile retrieved", response));
    }

    @PatchMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        UserResponse response = userService.updateProfile(request);
        return ResponseEntity.ok(ApiResponse.success("User profile updated successfully", response));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<Void>> deleteAccount() {
        userService.deleteAccount();
        return ResponseEntity.ok(ApiResponse.success("Account deleted successfully", null));
    }
}

