package com.qsl.tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {

    private String tokenName;
    private String tokenValue;
    private Long userId;
    private String username;
}
