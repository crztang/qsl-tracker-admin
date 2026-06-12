package com.qsl.tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QslPublicInfoResponse {

    private String trackingNo;
    private String callSign;
    private String status;
    private Boolean canConfirm;
}
