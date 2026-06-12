package com.qsl.tracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class QsoLogRequest {
    private Long id;

    @NotBlank
    private String callSign;
    @NotNull
    private LocalDateTime qsoTime;
    private String timezoneOffset = "+08:00";
    private BigDecimal frequencyMhz;
    private String bd;
    private String mode;
    private BigDecimal powerW;
    private String rstSent;
    private String rstReceived;
    private String antenna;
    private String country;
    private String qthProvince;
    private String qthCity;
    private String qthDistrict;
    private String qthDetail;
    private String remark;
}
