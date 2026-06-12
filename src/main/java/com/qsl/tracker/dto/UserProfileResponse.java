package com.qsl.tracker.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class UserProfileResponse {

    private Long id;
    private String username;
    private LocalDateTime lastLoginAt;
    private String callSign;
    private String email;
    private String mailingAddress;
    private String phone;
    private String recipient;
    private String postalCode;
}
