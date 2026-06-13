package com.qsl.tracker.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qsl.tracker.domain.QslShare;
import com.qsl.tracker.dto.QslShareIssueResponse;
import com.qsl.tracker.dto.QslShareRequest;
import com.qsl.tracker.dto.QslShareSummaryResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface QslShareService extends IService<QslShare> {

    QslShareSummaryResponse current();

    QslShareIssueResponse generate(QslShareRequest request, HttpServletRequest httpRequest);

    QslShareSummaryResponse updateSettings(QslShareRequest request);

    QslShareSummaryResponse revoke();

    String renderHtml(String token, HttpServletRequest request);
}
