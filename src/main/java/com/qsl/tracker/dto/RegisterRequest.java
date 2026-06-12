package com.qsl.tracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank
    @Size(min = 5, max = 64, message = "用户名长度不能少于5个字符")
    @Pattern(regexp = "^[^\\u4e00-\\u9fff]+$", message = "用户名不能包含中文")
    private String username;

    @NotBlank
    @Size(min = 8, max = 64)
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$")
    private String password;

    @NotBlank
    private String confirmPassword;

    @NotBlank
    private String captchaId;

    @NotBlank
    private String captchaCode;
}
