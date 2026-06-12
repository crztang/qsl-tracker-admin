package com.qsl.tracker.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("qso_log")
public class QsoLog {

    @TableId
    private Long id;
    private String callSign;
    private LocalDateTime qsoTime;
    private String timezoneOffset;
    private BigDecimal frequencyMhz;
    private String bd;
    private String mode;
    private BigDecimal powerW;
    private String rstSent;
    private String rstReceived;
    private String antenna;
    private String country;
    private String qthProvince;
    private String qthCity;
    private String qthDistrict;
    private String qthDetail;
    private String remark;
    @TableField(exist = false)
    private Boolean qslCardExists;
    @TableField(exist = false)
    private Long qslCardId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
