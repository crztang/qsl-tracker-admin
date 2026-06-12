package com.qsl.tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CaptchaResponse {

    private String captchaId;
    private String captchaImage;
    private long expiresIn;
}
