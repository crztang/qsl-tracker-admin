package com.qsl.tracker.dto;

import lombok.Data;

@Data
public class QslCardQuery {

    private long pageNo = 1;
    private long pageSize = 10;
    private String callSign;
    private String cardType;
    private String status;
}
