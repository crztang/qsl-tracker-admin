package com.qsl.tracker.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qsl.tracker.common.BusinessException;
import com.qsl.tracker.common.CurrentUserContext;
import com.qsl.tracker.common.PageResponse;
import com.qsl.tracker.domain.QslCard;
import com.qsl.tracker.domain.QsoLog;
import com.qsl.tracker.domain.SysDictItem;
import com.qsl.tracker.dto.QsoLogQuery;
import com.qsl.tracker.dto.QsoLogRequest;
import com.qsl.tracker.mapper.QsoLogMapper;
import com.qsl.tracker.service.DictService;
import com.qsl.tracker.service.DataScopeService;
import com.qsl.tracker.service.QslCardService;
import com.qsl.tracker.service.QsoLogService;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QsoLogServiceImpl extends ServiceImpl<QsoLogMapper, QsoLog> implements QsoLogService {

    private static final String MODE_DICT_CODE = "QSO_MODE";

    private final QslCardService qslCardService;
    private final DictService dictService;
    private final CurrentUserContext currentUserContext;
    private final DataScopeService dataScopeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QsoLog create(QsoLogRequest request) {
        QsoLog entity = new QsoLog();
        BeanUtils.copyProperties(request, entity);
        entity.setUserId(currentUserContext.userId());
        entity.setCallSign(request.getCallSign().trim().toUpperCase());
        entity.setTimezoneOffset(Optional.ofNullable(request.getTimezoneOffset())
                .filter(v -> !v.isBlank()).orElse("+08:00"));
        entity.setMode(normalizeMode(request.getMode()));
        validateMode(entity.getMode());
        save(entity);
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QsoLog update(Long id, QsoLogRequest request) {
        QsoLog entity = getAccessible(id);
        Long ownerId = entity.getUserId();
        BeanUtils.copyProperties(request, entity);
        entity.setId(id);
        entity.setUserId(ownerId);
        entity.setCallSign(request.getCallSign().trim().toUpperCase());
        entity.setTimezoneOffset(Optional.ofNullable(request.getTimezoneOffset())
                .filter(v -> !v.isBlank()).orElse("+08:00"));
        entity.setMode(normalizeMode(request.getMode()));
        validateMode(entity.getMode());
        updateById(entity);
        return entity;
    }

    @Override
    public PageResponse<QsoLog> page(QsoLogQuery query) {
        LambdaQueryWrapper<QsoLog> wrapper = new LambdaQueryWrapper<QsoLog>()
                .eq(!dataScopeService.canAccessAll(), QsoLog::getUserId, currentUserContext.userId())
                .like(query.getCallSign() != null && !query.getCallSign().isBlank(),
                        QsoLog::getCallSign, query.getCallSign())
                .eq(query.getMode() != null && !query.getMode().isBlank(), QsoLog::getMode, query.getMode())
                .like(query.getQth() != null && !query.getQth().isBlank(),
                        QsoLog::getQth, query.getQth())
                .ge(query.getStartTime() != null, QsoLog::getQsoTime, query.getStartTime())
                .le(query.getEndTime() != null, QsoLog::getQsoTime, query.getEndTime())
                .orderByDesc(QsoLog::getQsoTime);
        Page<QsoLog> page = page(new Page<>(query.getPageNo(), query.getPageSize()), wrapper);
        fillQslCardRelations(page.getRecords());
        return new PageResponse<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords());
    }

    @Override
    public QsoLog detail(Long id) {
        return getAccessible(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        QsoLog entity = getAccessible(id);
        qslCardService.update(new LambdaUpdateWrapper<QslCard>()
                .eq(QslCard::getQsoLogId, entity.getId())
                .eq(QslCard::getUserId, entity.getUserId())
                .set(QslCard::getQsoLogId, null));
        removeById(entity.getId());
    }

    private QsoLog getAccessible(Long id) {
        QsoLog entity = getOne(new LambdaQueryWrapper<QsoLog>()
                .eq(QsoLog::getId, id)
                .eq(!dataScopeService.canAccessAll(), QsoLog::getUserId, currentUserContext.userId())
                .last("limit 1"));
        if (entity == null) {
            throw new BusinessException("通联日志不存在");
        }
        return entity;
    }

    private void fillQslCardRelations(java.util.List<QsoLog> records) {
        if (records.isEmpty()) {
            return;
        }
        java.util.List<Long> qsoLogIds = records.stream().map(QsoLog::getId).toList();
        Map<Long, QslCard> cardByQsoLogId = qslCardService.list(new LambdaQueryWrapper<QslCard>()
                        .in(QslCard::getQsoLogId, qsoLogIds)
                        .eq(!dataScopeService.canAccessAll(), QslCard::getUserId, currentUserContext.userId())
                        .orderByDesc(QslCard::getCreatedAt))
                .stream()
                .filter(card -> card.getQsoLogId() != null)
                .collect(Collectors.toMap(QslCard::getQsoLogId, Function.identity(), (first, ignored) -> first));
        records.forEach(record -> {
            QslCard card = cardByQsoLogId.get(record.getId());
            record.setQslCardExists(card != null);
            record.setQslCardId(card == null ? null : card.getId());
        });
    }

    private String normalizeMode(String mode) {
        return Optional.ofNullable(mode)
                .map(String::trim)
                .filter(v -> !v.isBlank())
                .map(String::toUpperCase)
                .orElse(null);
    }

    private void validateMode(String mode) {
        if (mode == null) {
            return;
        }
        Set<String> allowedModes = dictService.listItems(MODE_DICT_CODE).stream()
                .map(SysDictItem::getItemCode)
                .collect(Collectors.toSet());
        if (!allowedModes.contains(mode)) {
            throw new BusinessException("模式不在字典配置中");
        }
    }
}
