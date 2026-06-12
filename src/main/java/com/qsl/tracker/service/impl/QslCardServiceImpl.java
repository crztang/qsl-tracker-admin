package com.qsl.tracker.service.impl;

import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.support.Crane4jTemplate;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qsl.tracker.common.BusinessException;
import com.qsl.tracker.common.PageResponse;
import com.qsl.tracker.common.TrackingNoUtil;
import com.qsl.tracker.domain.QslCard;
import com.qsl.tracker.domain.QslConfirmLog;
import com.qsl.tracker.domain.enums.CardStatus;
import com.qsl.tracker.domain.enums.CardType;
import com.qsl.tracker.dto.QslCardQuery;
import com.qsl.tracker.dto.QslCardRequest;
import com.qsl.tracker.dto.QslCardVO;
import com.qsl.tracker.dto.QslPublicInfoResponse;
import com.qsl.tracker.mapper.QslCardMapper;
import com.qsl.tracker.service.QslCardService;
import com.qsl.tracker.service.QslConfirmLogService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QslCardServiceImpl extends ServiceImpl<QslCardMapper, QslCard> implements QslCardService {

    private final QslConfirmLogService qslConfirmLogService;
    private final Crane4jTemplate crane4jTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QslCard create(QslCardRequest request) {
        if (request.getQsoLogId() != null && count(new LambdaQueryWrapper<QslCard>()
                .eq(QslCard::getQsoLogId, request.getQsoLogId())) > 0) {
            throw new BusinessException("该通联日志已存在QSL卡片");
        }
        QslCard entity = new QslCard();
        BeanUtils.copyProperties(request, entity);
        entity.setCallSign(request.getCallSign().trim().toUpperCase());
        if (entity.getStatus() == null) {
            entity.setStatus(CardType.RECEIVED.getCode().equals(request.getCardType())
                    ? CardStatus.RECEIVED.getValue()
                    : CardStatus.PENDING_SEND.getValue());
        }
        if (Boolean.TRUE.equals(entity.getPublicConfirmEnabled())
                && CardType.SENT.getCode().equals(request.getCardType())) {
            fillTracking(entity);
        }
        save(entity);
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QslCard update(Long id, QslCardRequest request) {
        QslCard entity = getById(id);
        if (entity == null) {
            throw new BusinessException("QSL卡片不存在");
        }
        String oldTrackingNo = entity.getTrackingNo();
        String oldToken = entity.getConfirmToken();
        BeanUtils.copyProperties(request, entity);
        entity.setId(id);
        entity.setCallSign(request.getCallSign().trim().toUpperCase());
        entity.setTrackingNo(oldTrackingNo);
        entity.setConfirmToken(oldToken);
        if (Boolean.TRUE.equals(entity.getPublicConfirmEnabled())
                && CardType.SENT.getCode().equals(entity.getCardType())
                && entity.getTrackingNo() == null) {
            fillTracking(entity);
        }
        updateById(entity);
        return entity;
    }

    @Override
    public PageResponse<QslCardVO> page(QslCardQuery query) {
        Page<QslCard> page = page(new Page<>(query.getPageNo(), query.getPageSize()), new LambdaQueryWrapper<QslCard>()
                .like(query.getCallSign() != null && !query.getCallSign().isBlank(), QslCard::getCallSign, query.getCallSign())
                .eq(query.getCardType() != null && !query.getCardType().isBlank(),
                        QslCard::getCardType, query.getCardType())
                .eq(query.getStatus() != null && !query.getStatus().isBlank(),
                        QslCard::getStatus, query.getStatus())
                .orderByDesc(QslCard::getCreatedAt));
        List<QslCardVO> records = page.getRecords().stream().map(entity -> {
            QslCardVO vo = new QslCardVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).toList();
        crane4jTemplate.execute(records);
        return new PageResponse<>(page.getTotal(), page.getCurrent(), page.getSize(), records);
    }

    @Override
    public QslPublicInfoResponse publicInfo(String trackingNo) {
        QslCard card = getByTrackingNo(trackingNo);
        boolean canConfirm = Boolean.TRUE.equals(card.getPublicConfirmEnabled())
                && !CardStatus.CONFIRMED.getValue().equals(card.getStatus());
        return new QslPublicInfoResponse(card.getTrackingNo(), card.getCallSign(), card.getStatus(), canConfirm);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirm(String trackingNo, String token, String ip, String userAgent) {
        QslCard card = getByTrackingNo(trackingNo);
        if (!Boolean.TRUE.equals(card.getPublicConfirmEnabled())) {
            saveConfirmLog(card, ip, userAgent, false, "公开确认已关闭");
            throw new BusinessException("公开确认已关闭");
        }
        if (!token.equals(card.getConfirmToken())) {
            saveConfirmLog(card, ip, userAgent, false, "确认令牌错误");
            throw new BusinessException("确认令牌错误");
        }
        card.setStatus(CardStatus.CONFIRMED.getValue());
        card.setConfirmedAt(LocalDateTime.now());
        card.setConfirmedIp(ip);
        card.setConfirmedUserAgent(userAgent);
        updateById(card);
        saveConfirmLog(card, ip, userAgent, true, null);
    }

    private QslCard getByTrackingNo(String trackingNo) {
        QslCard card = getOne(new LambdaQueryWrapper<QslCard>()
                .eq(QslCard::getTrackingNo, trackingNo)
                .last("limit 1"));
        if (card == null) {
            throw new BusinessException("QSL追踪号不存在");
        }
        return card;
    }

    private void fillTracking(QslCard entity) {
        for (int i = 0; i < 5; i++) {
            String trackingNo = TrackingNoUtil.newTrackingNo();
            long exists = count(new LambdaQueryWrapper<QslCard>().eq(QslCard::getTrackingNo, trackingNo));
            if (exists == 0) {
                entity.setTrackingNo(trackingNo);
                entity.setConfirmToken(TrackingNoUtil.newToken());
                return;
            }
        }
        throw new BusinessException("生成QSL追踪号失败");
    }

    private void saveConfirmLog(QslCard card, String ip, String userAgent, boolean success, String failReason) {
        QslConfirmLog log = new QslConfirmLog();
        log.setQslCardId(card.getId());
        log.setTrackingNo(card.getTrackingNo());
        log.setConfirmIp(ip);
        log.setUserAgent(userAgent);
        log.setSuccess(success);
        log.setFailReason(failReason);
        qslConfirmLogService.save(log);
    }
}
