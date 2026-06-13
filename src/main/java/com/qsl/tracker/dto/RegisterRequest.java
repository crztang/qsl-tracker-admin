package com.qsl.tracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank
    @Size(min = 5, max = 64, message = "用户名长度不能少于5个字符")
    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "用户名只能包含英文字母和数字")
    private String username;

    @NotBlank
    @Size(min = 6, max = 64)
    @Pattern(regexp = "^(?!\\d+$).+$", message = "密码不能为纯数字")
    private String password;

    @NotBlank
    private String confirmPassword;

    @NotBlank
    private String captchaId;

    @NotBlank
    private String captchaCode;
}
