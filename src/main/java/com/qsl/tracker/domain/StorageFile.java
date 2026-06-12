package com.qsl.tracker.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("storage_file")
public class StorageFile {

    @TableId
    private Long id;
    private Long userId;
    private String fileKey;
    private String originalName;
    private String storageName;
    private String relativePath;
    private String fileExtension;
    private String contentType;
    private Long fileSize;
    private String fileHash;
    private String status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
