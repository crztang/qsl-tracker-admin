package com.qsl.tracker.dto;

import lombok.Data;

@Data
public class QslShareIssueResponse extends QslShareSummaryResponse {

    private String token;
    private String embedUrl;
    private String iframeCode;
}
