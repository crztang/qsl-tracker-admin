package com.qsl.tracker.service;

import com.qsl.tracker.dto.ChangePasswordRequest;
import com.qsl.tracker.dto.LoginRequest;
import com.qsl.tracker.dto.LoginResponse;
import com.qsl.tracker.dto.RegisterRequest;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {

    LoginResponse login(LoginRequest request, HttpServletRequest httpRequest);

    void register(RegisterRequest request, HttpServletRequest httpRequest);

    void changePassword(ChangePasswordRequest request);

    void logout();
}
