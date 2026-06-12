package com.qsl.tracker.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("qsl_confirm_log")
public class QslConfirmLog {

    @TableId
    private Long id;
    private Long qslCardId;
    private String trackingNo;
    private String confirmIp;
    private String userAgent;
    private Boolean success;
    private String failReason;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
