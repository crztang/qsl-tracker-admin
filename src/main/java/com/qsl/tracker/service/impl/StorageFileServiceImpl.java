package com.qsl.tracker.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qsl.tracker.common.BusinessException;
import com.qsl.tracker.domain.StorageFile;
import com.qsl.tracker.dto.StorageFileResponse;
import com.qsl.tracker.mapper.StorageFileMapper;
import com.qsl.tracker.service.StorageFileService;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.DigestInputStream;
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

        String storageName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String relativePath = "print-template/" + datePath + "/" + storageName;
        Path target = rootPath().resolve(relativePath).normalize();
        ensureInsideRoot(target);

        try {
            Files.createDirectories(target.getParent());
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream input = new DigestInputStream(file.getInputStream(), digest)) {
                Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
            }
            StorageFile entity = new StorageFile();
            entity.setOriginalName(originalName);
            entity.setStorageName(storageName);
            entity.setRelativePath(relativePath);
            entity.setFileExtension(extension);
            entity.setContentType(contentType);
            entity.setFileSize(file.getSize());
            entity.setFileHash(HexFormat.of().formatHex(digest.digest()));
            entity.setStatus("1");
            save(entity);

            StorageFileResponse response = new StorageFileResponse();
            response.setId(entity.getId());
            response.setOriginalName(entity.getOriginalName());
            response.setContentType(entity.getContentType());
            response.setFileSize(entity.getFileSize());
            return response;
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new BusinessException("图片保存失败");
        }
    }

    @Override
    public StorageFile getAvailableFile(Long id) {
        StorageFile file = getById(id);
        if (file == null || !"1".equals(file.getStatus())) {
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
