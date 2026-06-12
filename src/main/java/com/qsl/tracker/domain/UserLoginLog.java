package com.qsl.tracker.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("user_login_log")
public class UserLoginLog {

    @TableId
    private Long id;
    private Long userId;
    private String username;
    private String loginIp;
    private String userAgent;
    private Boolean success;
    private String failReason;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
