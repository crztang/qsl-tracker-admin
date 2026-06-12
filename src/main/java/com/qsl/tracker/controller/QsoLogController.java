package com.qsl.tracker.controller;

import com.qsl.tracker.common.ApiResponse;
import com.qsl.tracker.common.BusinessException;
import com.qsl.tracker.common.PageResponse;
import com.qsl.tracker.domain.QsoLog;
import com.qsl.tracker.dto.QsoLogQuery;
import com.qsl.tracker.dto.QsoLogRequest;
import com.qsl.tracker.service.QsoLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/qso-logs")
public class QsoLogController {

    private final QsoLogService qsoLogService;

    @GetMapping
    public ApiResponse<PageResponse<QsoLog>> page(QsoLogQuery query) {
        return ApiResponse.ok(qsoLogService.page(query));
    }

    @GetMapping("/detail")
    public ApiResponse<QsoLog> detail(@RequestParam Long id) {
        QsoLog entity = qsoLogService.getById(id);
        if (entity == null) {
            throw new BusinessException("通联日志不存在");
        }
        return ApiResponse.ok(entity);
    }

    @PostMapping
    public ApiResponse<QsoLog> create(@Valid @RequestBody QsoLogRequest request) {
        return ApiResponse.ok(qsoLogService.create(request));
    }

    @PostMapping("/update")
    public ApiResponse<QsoLog> update( @Valid @RequestBody QsoLogRequest request) {
        return ApiResponse.ok(qsoLogService.update(request.getId(), request));
    }

    @PostMapping("/delete")
    public ApiResponse<Void> delete( @RequestBody QsoLogRequest request) {
        if (!qsoLogService.removeById(request.getId())) {
            throw new BusinessException("通联日志不存在");
        }
        return ApiResponse.ok();
    }
}
