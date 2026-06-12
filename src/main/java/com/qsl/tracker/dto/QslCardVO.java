package com.qsl.tracker.dto;

import cn.crane4j.annotation.AssembleEnum;
import cn.crane4j.annotation.ContainerEnum;
import cn.crane4j.annotation.Mapping;
import com.qsl.tracker.domain.enums.CardStatus;
import com.qsl.tracker.domain.enums.CardType;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class QslCardVO {

    private Long id;
    @AssembleEnum(type = CardType.class,
            props = @Mapping(ref = "cardTypeStr"),
            followTypeConfig = false,
            enums = @ContainerEnum(
                    key = "code",
                    value = "value"
            ))
    private String cardType;
    private String cardTypeStr;
    private Long qsoLogId;
    private String callSign;
    private String contactName;
    private String contactAddress;
    private String postalCode;
    @AssembleEnum(type = CardStatus.class,
            props = @Mapping(ref = "statusStr"),
            followTypeConfig = false,
            enums = @ContainerEnum(
                    key = "code",
                    value = "value"))
    private String status;
    private String statusStr;
    private String trackingNo;
    private String confirmToken;
    private Boolean publicConfirmEnabled;
    private LocalDateTime sentAt;
    private LocalDateTime receivedAt;
    private LocalDateTime confirmedAt;
    private String confirmedIp;
    private String confirmedUserAgent;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
