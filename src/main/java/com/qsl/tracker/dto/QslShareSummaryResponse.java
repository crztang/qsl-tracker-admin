package com.qsl.tracker.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class QslShareSummaryResponse {

    private Boolean enabled;
    private Integer recordLimit;
    private String expiryPreset;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean expired;
    private boolean hasToken;
}
