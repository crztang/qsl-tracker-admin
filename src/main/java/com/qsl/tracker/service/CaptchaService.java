package com.qsl.tracker.service;

import com.qsl.tracker.dto.CaptchaResponse;

public interface CaptchaService {

    CaptchaResponse generate(String scene, String ip);

    void verify(String scene, String captchaId, String captchaCode, String ip);
}
