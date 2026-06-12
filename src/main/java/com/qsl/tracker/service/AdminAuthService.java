package com.qsl.tracker.service;

import com.qsl.tracker.dto.LoginRequest;
import com.qsl.tracker.dto.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface AdminAuthService {

    LoginResponse login(LoginRequest request, HttpServletRequest httpRequest);

    void logout();
}
