package com.qsl.tracker.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CardStatus {
    PENDING_SEND("1", "待发出"),
    SENT("2", "已发出"),
    RECEIVED("3", "已收到"),
    CONFIRMED("4", "已确认");

    private final String code;
    private final String value;


}
