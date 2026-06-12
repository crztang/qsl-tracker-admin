package com.qsl.tracker.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class QsoLogQuery {

    private long pageNo = 1;
    private long pageSize = 10;
    private String callSign;
    private String mode;
    private String qth;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
