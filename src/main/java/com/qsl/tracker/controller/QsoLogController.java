package com.qsl.tracker.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.qsl.tracker.common.ApiResponse;
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
    @SaCheckPermission("qso:read")
    public ApiResponse<PageResponse<QsoLog>> page(QsoLogQuery query) {
        return ApiResponse.ok(qsoLogService.page(query));
    }

    @GetMapping("/detail")
    @SaCheckPermission("qso:read")
    public ApiResponse<QsoLog> detail(@RequestParam Long id) {
        return ApiResponse.ok(qsoLogService.detail(id));
    }

    @PostMapping
    @SaCheckPermission("qso:write")
    public ApiResponse<QsoLog> create(@Valid @RequestBody QsoLogRequest request) {
        return ApiResponse.ok(qsoLogService.create(request));
    }

    @PostMapping("/update")
    @SaCheckPermission("qso:write")
    public ApiResponse<QsoLog> update(@Valid @RequestBody QsoLogRequest request) {
        return ApiResponse.ok(qsoLogService.update(request.getId(), request));
    }

    @PostMapping("/delete")
    @SaCheckPermission("qso:write")
    public ApiResponse<Void> delete(@RequestBody QsoLogRequest request) {
        qsoLogService.delete(request.getId());
        return ApiResponse.ok();
    }
}
