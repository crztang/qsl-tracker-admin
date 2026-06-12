package com.qsl.tracker.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.qsl.tracker.common.ApiResponse;
import com.qsl.tracker.common.WebUtil;
import com.qsl.tracker.dto.CaptchaResponse;
import com.qsl.tracker.dto.ChangePasswordRequest;
import com.qsl.tracker.dto.LoginRequest;
import com.qsl.tracker.dto.LoginResponse;
import com.qsl.tracker.dto.RegisterRequest;
import com.qsl.tracker.service.AuthService;
import com.qsl.tracker.service.CaptchaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final CaptchaService captchaService;

    @GetMapping("/captcha")
    public ApiResponse<CaptchaResponse> captcha(
            @RequestParam String scene,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(captchaService.generate(scene, WebUtil.clientIp(httpRequest)));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(authService.login(request, httpRequest));
    }

    @PostMapping("/register")
    public ApiResponse<Void> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {
        authService.register(request, httpRequest);
        return ApiResponse.ok();
    }

    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ApiResponse.ok();
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        authService.logout();
        return ApiResponse.ok();
    }

    @GetMapping("/me")
    public ApiResponse<Object> me() {
        return ApiResponse.ok(StpUtil.getLoginId());
    }
}
