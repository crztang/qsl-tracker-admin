package com.qsl.tracker.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("sys_user")
public class User {

    @TableId
    private Long id;
    private String username;
    private String callSign;
    private String email;
    private String mailingAddress;
    private String phone;
    private String recipient;
    private String postalCode;
    private String passwordHash;
    private String passwordSalt;
    private Boolean enabled;
    private Integer failedLoginCount;
    private LocalDateTime lockedUntil;
    private LocalDateTime lastLoginAt;
    private String lastLoginIp;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
