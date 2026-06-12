package com.qsl.tracker.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qsl.tracker.domain.StorageFile;
import com.qsl.tracker.dto.StorageFileResponse;
import java.nio.file.Path;
import org.springframework.web.multipart.MultipartFile;

public interface StorageFileService extends IService<StorageFile> {

    StorageFileResponse uploadImage(MultipartFile file);

    StorageFile getAccessibleFile(String fileKey);

    StorageFile getFileForOwner(String fileKey, Long ownerId);

    Path resolveFile(StorageFile file);
}
