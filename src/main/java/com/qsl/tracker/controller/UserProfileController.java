package com.qsl.tracker.controller;

import com.qsl.tracker.common.ApiResponse;
import com.qsl.tracker.dto.UserProfileRequest;
import com.qsl.tracker.dto.UserProfileResponse;
import com.qsl.tracker.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profile")
public class UserProfileController {

    private final UserService userService;

    @GetMapping
    public ApiResponse<UserProfileResponse> detail() {
        return ApiResponse.ok(userService.currentProfile());
    }

    @PostMapping("/update")
    public ApiResponse<UserProfileResponse> update(
            @Valid @RequestBody UserProfileRequest request) {
        return ApiResponse.ok(userService.updateCurrentProfile(request));
    }
}
