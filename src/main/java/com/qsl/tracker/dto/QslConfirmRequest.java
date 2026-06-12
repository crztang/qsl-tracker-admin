package com.qsl.tracker.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class QslConfirmRequest {

    @NotBlank
    private String token;
}
