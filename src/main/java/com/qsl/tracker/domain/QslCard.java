package com.qsl.tracker.domain;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("qsl_card")
public class QslCard {

    @TableId
    private Long id;
    private String cardType;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Long qsoLogId;
    private String callSign;
    private String contactName;
    private String contactAddress;
    private String postalCode;
    private String status;
    private String trackingNo;
    private String confirmToken;
    private Boolean publicConfirmEnabled;
    private LocalDateTime sentAt;
    private LocalDateTime receivedAt;
    private LocalDateTime confirmedAt;
    private String confirmedIp;
    private String confirmedUserAgent;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
