package com.qsl.tracker.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "qsl.captcha")
public class CaptchaProperties {

    private int width = 130;
    private int height = 48;
    private int difficulty = 50;
    private long ttlSeconds = 120;
    private int rateWindowSeconds = 60;
    private int generateLimit = 20;
    private int verifyLimit = 30;
}
