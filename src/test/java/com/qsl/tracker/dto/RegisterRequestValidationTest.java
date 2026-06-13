package com.qsl.tracker.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.Test;

class RegisterRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void usernameShouldOnlyAllowLettersAndDigits() {
        RegisterRequest request = validRequest();
        request.setUsername("测试user");

        Set<String> messages = validator.validateProperty(request, "username").stream()
                .map(violation -> violation.getMessage())
                .collect(java.util.stream.Collectors.toSet());

        assertThat(messages).contains("用户名只能包含英文字母和数字");
    }

    @Test
    void passwordShouldAcceptSixCharactersWhenOtherRulesPass() {
        RegisterRequest request = validRequest();
        request.setPassword("a1b2c3");
        request.setConfirmPassword("a1b2c3");

        assertThat(validator.validateProperty(request, "password")).isEmpty();
    }

    @Test
    void passwordShouldRejectFiveCharacters() {
        RegisterRequest request = validRequest();
        request.setPassword("a1b2c");
        request.setConfirmPassword("a1b2c");

        assertThat(validator.validateProperty(request, "password")).isNotEmpty();
    }

    private RegisterRequest validRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("abcde");
        request.setPassword("a1b2c3");
        request.setConfirmPassword("a1b2c3");
        request.setCaptchaId("captcha-id");
        request.setCaptchaCode("1234");
        return request;
    }
}
