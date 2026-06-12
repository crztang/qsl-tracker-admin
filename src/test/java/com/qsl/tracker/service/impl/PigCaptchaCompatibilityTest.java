package com.qsl.tracker.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.pig4cloud.captcha.ArithmeticCaptcha;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class PigCaptchaCompatibilityTest {

    private static final Pattern OPERAND_PATTERN = Pattern.compile("\\d+");

    @Test
    void rendersArithmeticCaptchaOnJdk17() {
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(130, 48);
        captcha.setLen(2);
        captcha.setDifficulty(50);
        captcha.supportAlgorithmSign(3);

        assertThat(captcha.text()).matches("\\d+");
        assertThat(captcha.getArithmeticString()).endsWith("=?");
        assertThat(captcha.toBase64()).startsWith("data:image/png;base64,");

        Matcher matcher = OPERAND_PATTERN.matcher(captcha.getArithmeticString());
        while (matcher.find()) {
            assertThat(Integer.parseInt(matcher.group())).isLessThan(50);
        }
    }
}
