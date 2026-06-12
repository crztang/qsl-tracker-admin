package com.qsl.tracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserProfileRequest {

    @Size(max = 32)
    private String callSign;
    @Email
    @Size(max = 128)
    private String email;
    @Size(max = 500)
    private String mailingAddress;
    @Size(max = 32)
    private String phone;
    @Size(max = 128)
    private String recipient;
    @Size(max = 32)
    private String postalCode;
}
