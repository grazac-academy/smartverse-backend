package org.smartvert.smartvert.controller;

import lombok.RequiredArgsConstructor;
import org.smartvert.smartvert.model.dto.ApiResponse;
import org.smartvert.smartvert.model.dto.UserResponse;
import org.smartvert.smartvert.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
}
