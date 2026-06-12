package com.qsl.tracker.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qsl.tracker.common.BusinessException;
import com.qsl.tracker.common.PageResponse;
import com.qsl.tracker.domain.QslCard;
import com.qsl.tracker.domain.QsoLog;
import com.qsl.tracker.dto.QsoLogQuery;
import com.qsl.tracker.dto.QsoLogRequest;
import com.qsl.tracker.mapper.QsoLogMapper;
import com.qsl.tracker.service.QslCardService;
import com.qsl.tracker.service.QsoLogService;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QsoLogServiceImpl extends ServiceImpl<QsoLogMapper, QsoLog> implements QsoLogService {

    private final QslCardService qslCardService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QsoLog create(QsoLogRequest request) {
        QsoLog entity = new QsoLog();
        BeanUtils.copyProperties(request, entity);
        entity.setCallSign(request.getCallSign().trim().toUpperCase());
        entity.setTimezoneOffset(Optional.ofNullable(request.getTimezoneOffset()).filter(v -> !v.isBlank()).orElse("+08:00"));
        save(entity);
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QsoLog update(Long id, QsoLogRequest request) {
        QsoLog entity = getById(id);
        if (entity == null) {
            throw new BusinessException("通联日志不存在");
        }
        BeanUtils.copyProperties(request, entity);
        entity.setId(id);
        entity.setCallSign(request.getCallSign().trim().toUpperCase());
        entity.setTimezoneOffset(Optional.ofNullable(request.getTimezoneOffset()).filter(v -> !v.isBlank()).orElse("+08:00"));
        updateById(entity);
        return entity;
    }

    @Override
    public PageResponse<QsoLog> page(QsoLogQuery query) {
        Page<QsoLog> page = page(new Page<>(query.getPageNo(), query.getPageSize()), new LambdaQueryWrapper<QsoLog>()
                .like(query.getCallSign() != null && !query.getCallSign().isBlank(), QsoLog::getCallSign, query.getCallSign())
                .eq(query.getMode() != null && !query.getMode().isBlank(), QsoLog::getMode, query.getMode())
                .eq(query.getCountry() != null && !query.getCountry().isBlank(), QsoLog::getCountry, query.getCountry())
                .ge(query.getStartTime() != null, QsoLog::getQsoTime, query.getStartTime())
                .le(query.getEndTime() != null, QsoLog::getQsoTime, query.getEndTime())
                .orderByDesc(QsoLog::getQsoTime));
        fillQslCardRelations(page.getRecords());
        return new PageResponse<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords());
    }

    private void fillQslCardRelations(java.util.List<QsoLog> records) {
        if (records.isEmpty()) {
            return;
        }
        java.util.List<Long> qsoLogIds = records.stream().map(QsoLog::getId).toList();
        Map<Long, QslCard> cardByQsoLogId = qslCardService.list(new LambdaQueryWrapper<QslCard>()
                        .in(QslCard::getQsoLogId, qsoLogIds)
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
}
