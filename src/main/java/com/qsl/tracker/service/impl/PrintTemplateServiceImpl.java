package com.qsl.tracker.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qsl.tracker.common.BusinessException;
import com.qsl.tracker.common.CurrentUserContext;
import com.qsl.tracker.domain.PrintTemplate;
import com.qsl.tracker.domain.StorageFile;
import com.qsl.tracker.dto.PrintTemplateRequest;
import com.qsl.tracker.dto.PrintTemplateResponse;
import com.qsl.tracker.mapper.PrintTemplateMapper;
import com.qsl.tracker.service.DataScopeService;
import com.qsl.tracker.service.PrintTemplateService;
import com.qsl.tracker.service.StorageFileService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PrintTemplateServiceImpl extends ServiceImpl<PrintTemplateMapper, PrintTemplate>
        implements PrintTemplateService {

    private final StorageFileService storageFileService;
    private final ObjectMapper objectMapper;
    private final CurrentUserContext currentUserContext;
    private final DataScopeService dataScopeService;

    @Override
    public List<PrintTemplateResponse> listByUser(String templateType) {
        return list(new LambdaQueryWrapper<PrintTemplate>()
                .eq(!dataScopeService.canAccessAll(), PrintTemplate::getUserId, currentUserContext.userId())
                .eq(templateType != null && !templateType.isBlank(),
                        PrintTemplate::getTemplateType, templateType)
                .orderByDesc(PrintTemplate::getIsDefault)
                .orderByAsc(PrintTemplate::getSortOrder)
                .orderByDesc(PrintTemplate::getUpdatedAt))
                .stream().map(this::toResponse).toList();
    }

    @Override
    public PrintTemplateResponse detail(Long id) {
        return toResponse(getAccessibleTemplate(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PrintTemplateResponse create(PrintTemplateRequest request) {
        Long userId = currentUserContext.userId();
        StorageFile background = resolveBackground(request.getBackgroundFileKey(), userId);
        PrintTemplate entity = new PrintTemplate();
        copyRequest(request, entity);
        entity.setUserId(userId);
        entity.setBackgroundFileId(background == null ? null : background.getId());
        if (entity.getIsDefault()) {
            clearDefault(entity.getUserId(), entity.getTemplateType(), null);
        }
        save(entity);
        return toResponse(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PrintTemplateResponse update(PrintTemplateRequest request) {
        if (request.getId() == null) {
            throw new BusinessException("打印配置ID不能为空");
        }
        PrintTemplate entity = getAccessibleTemplate(request.getId());
        StorageFile background = resolveBackground(request.getBackgroundFileKey(), entity.getUserId());
        Long ownerId = entity.getUserId();
        copyRequest(request, entity);
        entity.setUserId(ownerId);
        entity.setBackgroundFileId(background == null ? null : background.getId());
        if (Boolean.TRUE.equals(entity.getIsDefault())) {
            clearDefault(entity.getUserId(), entity.getTemplateType(), entity.getId());
        }
        updateById(entity);
        return toResponse(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (id == null) {
            throw new BusinessException("打印配置ID不能为空");
        }
        removeById(getAccessibleTemplate(id).getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefault(Long id) {
        if (id == null) {
            throw new BusinessException("打印配置ID不能为空");
        }
        PrintTemplate entity = getAccessibleTemplate(id);
        clearDefault(entity.getUserId(), entity.getTemplateType(), entity.getId());
        entity.setIsDefault(true);
        updateById(entity);
    }

    private void copyRequest(PrintTemplateRequest request, PrintTemplate entity) {
        if (!request.getConfigJson().isObject()) {
            throw new BusinessException("打印配置格式不正确");
        }
        entity.setTemplateName(request.getTemplateName().trim());
        entity.setTemplateType(request.getTemplateType());
        entity.setConfigJson(writeJson(request.getConfigJson()));
        entity.setEnabled(request.getEnabled() == null || request.getEnabled());
        entity.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()));
        entity.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        entity.setRemark(request.getRemark());
    }

    private void clearDefault(Long userId, String templateType, Long excludedId) {
        List<PrintTemplate> defaults = list(new LambdaQueryWrapper<PrintTemplate>()
                .eq(PrintTemplate::getUserId, userId)
                .eq(PrintTemplate::getTemplateType, templateType)
                .eq(PrintTemplate::getIsDefault, true)
                .ne(excludedId != null, PrintTemplate::getId, excludedId));
        defaults.forEach(item -> item.setIsDefault(false));
        updateBatchById(defaults);
    }

    private PrintTemplate getAccessibleTemplate(Long id) {
        PrintTemplate entity = getOne(new LambdaQueryWrapper<PrintTemplate>()
                .eq(PrintTemplate::getId, id)
                .eq(!dataScopeService.canAccessAll(), PrintTemplate::getUserId, currentUserContext.userId())
                .last("limit 1"));
        if (entity == null) {
            throw new BusinessException("打印配置不存在");
        }
        return entity;
    }

    private StorageFile resolveBackground(String fileKey, Long ownerId) {
        return fileKey == null || fileKey.isBlank()
                ? null : storageFileService.getFileForOwner(fileKey, ownerId);
    }

    private PrintTemplateResponse toResponse(PrintTemplate entity) {
        PrintTemplateResponse response = new PrintTemplateResponse();
        BeanUtils.copyProperties(entity, response, "backgroundFileId");
        response.setConfigJson(readJson(entity.getConfigJson()));
        if (entity.getBackgroundFileId() != null) {
            StorageFile file = storageFileService.getById(entity.getBackgroundFileId());
            if (file != null && entity.getUserId().equals(file.getUserId()) && "1".equals(file.getStatus())) {
                response.setBackgroundFileKey(file.getFileKey());
            }
        }
        return response;
    }

    private String writeJson(JsonNode value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new BusinessException("打印配置格式不正确");
        }
    }

    private JsonNode readJson(String value) {
        try {
            return objectMapper.readTree(value);
        } catch (JsonProcessingException e) {
            throw new BusinessException("打印配置格式不正确");
        }
    }
}
