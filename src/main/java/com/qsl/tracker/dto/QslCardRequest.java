package com.qsl.tracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class QslCardRequest {
    private Long id;

    @NotNull
    @Pattern(regexp = "[12]")
    private String cardType;
    private Long qsoLogId;
    @NotBlank
    private String callSign;
    private String contactName;
    private String contactAddress;
    private String postalCode;
    @Pattern(regexp = "[1-4]")
    private String status;
    private Boolean publicConfirmEnabled = true;
    private LocalDateTime sentAt;
    private LocalDateTime receivedAt;
    private String remark;
}
