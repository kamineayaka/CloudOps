package com.cloudops.user.controller;

import com.cloudops.common.dto.ApiResponse;
import com.cloudops.user.dto.LoginRequest;
import com.cloudops.user.dto.LoginResponse;
import com.cloudops.user.dto.RefreshTokenRequest;
import com.cloudops.user.dto.UserProfileResponse;
import com.cloudops.user.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        authService.logout();
        return ApiResponse.ok("已退出登录", null);
    }

    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> me() {
        return ApiResponse.ok(authService.currentUser());
    }
}
