package com.qsl.tracker.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class QslShareRequest {

    @Min(1)
    @Max(50)
    private Integer recordLimit = 10;

    @Pattern(regexp = "^(permanent|1d|7d|30d)$")
    private String expiryPreset = "7d";

    private Boolean enabled = Boolean.TRUE;
}
