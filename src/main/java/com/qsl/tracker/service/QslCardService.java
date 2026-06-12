package com.qsl.tracker.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qsl.tracker.common.PageResponse;
import com.qsl.tracker.domain.QslCard;
import com.qsl.tracker.dto.QslCardQuery;
import com.qsl.tracker.dto.QslCardRequest;
import com.qsl.tracker.dto.QslCardVO;
import com.qsl.tracker.dto.QslPublicInfoResponse;

public interface QslCardService extends IService<QslCard> {

    QslCard create(QslCardRequest request);

    QslCard update(Long id, QslCardRequest request);

    PageResponse<QslCardVO> page(QslCardQuery query);

    QslPublicInfoResponse publicInfo(String trackingNo);

    void confirm(String trackingNo, String token, String ip, String userAgent);
}
