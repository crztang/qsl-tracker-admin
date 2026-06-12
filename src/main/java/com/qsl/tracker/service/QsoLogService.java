package com.qsl.tracker.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qsl.tracker.common.PageResponse;
import com.qsl.tracker.domain.QsoLog;
import com.qsl.tracker.dto.QsoLogQuery;
import com.qsl.tracker.dto.QsoLogRequest;

public interface QsoLogService extends IService<QsoLog> {

    QsoLog create(QsoLogRequest request);

    QsoLog update(Long id, QsoLogRequest request);

    PageResponse<QsoLog> page(QsoLogQuery query);

    QsoLog detail(Long id);

    void delete(Long id);
}
