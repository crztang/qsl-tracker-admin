package com.qsl.tracker.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CardType {
    SENT("1", "发出"),
    RECEIVED("2", "收到");

    private final String code;
    private final String value;


}
