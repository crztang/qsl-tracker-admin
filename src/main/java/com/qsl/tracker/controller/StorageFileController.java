package com.qsl.tracker.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.qsl.tracker.common.ApiResponse;
import com.qsl.tracker.domain.StorageFile;
import com.qsl.tracker.dto.StorageFileResponse;
import com.qsl.tracker.service.StorageFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files")
public class StorageFileController {

    private final StorageFileService storageFileService;

    @PostMapping("/upload")
    @SaCheckPermission("file:write")
    public ApiResponse<StorageFileResponse> upload(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(storageFileService.uploadImage(file));
    }

    @GetMapping("/{fileKey}/content")
    @SaCheckPermission("file:read")
    public ResponseEntity<Resource> content(@PathVariable String fileKey) {
        StorageFile file = storageFileService.getAccessibleFile(fileKey);
        Resource resource = new FileSystemResource(storageFileService.resolveFile(file));
        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(file.getContentType());
        } catch (Exception ignored) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }
        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(file.getFileSize())
                .cacheControl(CacheControl.noCache())
                .body(resource);
    }
}
