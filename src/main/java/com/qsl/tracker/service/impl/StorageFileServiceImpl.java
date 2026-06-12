package com.qsl.tracker.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qsl.tracker.common.BusinessException;
import com.qsl.tracker.common.CurrentUserContext;
import com.qsl.tracker.domain.StorageFile;
import com.qsl.tracker.dto.StorageFileResponse;
import com.qsl.tracker.mapper.StorageFileMapper;
import com.qsl.tracker.service.DataScopeService;
import com.qsl.tracker.service.StorageFileService;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class StorageFileServiceImpl extends ServiceImpl<StorageFileMapper, StorageFile>
        implements StorageFileService {

    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    private final CurrentUserContext currentUserContext;
    private final DataScopeService dataScopeService;

    @Value("${qsl.storage.local-root}")
    private String localRoot;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StorageFileResponse uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择图片");
        }
        String originalName = file.getOriginalFilename() == null ? "image" : file.getOriginalFilename();
        if (originalName.length() > 255) {
            throw new BusinessException("文件名过长");
        }
        String extension = extensionOf(originalName);
        String contentType = file.getContentType();
        if (!IMAGE_EXTENSIONS.contains(extension)
                || contentType == null
                || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new BusinessException("仅支持图片文件");
        }

        Long userId = currentUserContext.userId();
        String fileKey = UUID.randomUUID().toString().replace("-", "");
        String storageName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String relativePath = "users/" + userId + "/print-template/" + datePath + "/" + storageName;
        Path target = rootPath().resolve(relativePath).normalize();
        ensureInsideRoot(target);

        try {
            Files.createDirectories(target.getParent());
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream input = new DigestInputStream(file.getInputStream(), digest)) {
                Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
            }
            StorageFile entity = new StorageFile();
            entity.setUserId(userId);
            entity.setFileKey(fileKey);
            entity.setOriginalName(originalName);
            entity.setStorageName(storageName);
            entity.setRelativePath(relativePath);
            entity.setFileExtension(extension);
            entity.setContentType(contentType);
            entity.setFileSize(file.getSize());
            entity.setFileHash(HexFormat.of().formatHex(digest.digest()));
            entity.setStatus("1");
            save(entity);
            return toResponse(entity);
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new BusinessException("图片保存失败");
        }
    }

    @Override
    public StorageFile getAccessibleFile(String fileKey) {
        StorageFile file = getOne(new LambdaQueryWrapper<StorageFile>()
                .eq(StorageFile::getFileKey, fileKey)
                .eq(StorageFile::getStatus, "1")
                .eq(!dataScopeService.canAccessAll(), StorageFile::getUserId, currentUserContext.userId())
                .last("limit 1"));
        if (file == null) {
            throw new BusinessException("文件不存在");
        }
        return file;
    }

    @Override
    public StorageFile getFileForOwner(String fileKey, Long ownerId) {
        if (fileKey == null || fileKey.isBlank()) {
            return null;
        }
        StorageFile file = getOne(new LambdaQueryWrapper<StorageFile>()
                .eq(StorageFile::getFileKey, fileKey)
                .eq(StorageFile::getUserId, ownerId)
                .eq(StorageFile::getStatus, "1")
                .last("limit 1"));
        if (file == null) {
            throw new BusinessException("文件不存在");
        }
        return file;
    }

    @Override
    public Path resolveFile(StorageFile file) {
        Path path = rootPath().resolve(file.getRelativePath()).normalize();
        ensureInsideRoot(path);
        if (!Files.isRegularFile(path)) {
            throw new BusinessException("文件不存在");
        }
        return path;
    }

    private StorageFileResponse toResponse(StorageFile entity) {
        StorageFileResponse response = new StorageFileResponse();
        response.setFileKey(entity.getFileKey());
        response.setOriginalName(entity.getOriginalName());
        response.setContentType(entity.getContentType());
        response.setFileSize(entity.getFileSize());
        return response;
    }

    private Path rootPath() {
        return Path.of(localRoot).toAbsolutePath().normalize();
    }

    private void ensureInsideRoot(Path path) {
        if (!path.startsWith(rootPath())) {
            throw new BusinessException("文件路径不正确");
        }
    }

    private String extensionOf(String filename) {
        int index = filename.lastIndexOf('.');
        return index < 0 ? "" : filename.substring(index + 1).toLowerCase(Locale.ROOT);
    }
}
