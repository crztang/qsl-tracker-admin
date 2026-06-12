package com.qsl.tracker.dto;

import lombok.Data;

@Data
public class StorageFileResponse {

    private Long id;
    private String originalName;
    private String contentType;
    private Long fileSize;
}
