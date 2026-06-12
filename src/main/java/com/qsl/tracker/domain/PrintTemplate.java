package com.qsl.tracker.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("print_template")
public class PrintTemplate {

    @TableId
    private Long id;
    private Long adminUserId;
    private String templateName;
    private String templateType;
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Long backgroundFileId;
    private String configJson;
    private Boolean enabled;
    private Boolean isDefault;
    private Integer sortOrder;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
