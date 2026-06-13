package com.qsl.tracker.controller;

import com.qsl.tracker.common.ApiResponse;
import com.qsl.tracker.dto.QslShareIssueResponse;
import com.qsl.tracker.dto.QslShareRequest;
import com.qsl.tracker.dto.QslShareSummaryResponse;
import com.qsl.tracker.service.QslShareService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profile/embed-share")
public class QslShareController {

    private final QslShareService qslShareService;

    @GetMapping
    public ApiResponse<QslShareSummaryResponse> current() {
        return ApiResponse.ok(qslShareService.current());
    }

    @PostMapping("/generate")
    public ApiResponse<QslShareIssueResponse> generate(
            @Valid @RequestBody QslShareRequest request,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(qslShareService.generate(request, httpRequest));
    }

    @PostMapping("/update")
    public ApiResponse<QslShareSummaryResponse> update(@Valid @RequestBody QslShareRequest request) {
        return ApiResponse.ok(qslShareService.updateSettings(request));
    }

    @PostMapping("/revoke")
    public ApiResponse<QslShareSummaryResponse> revoke() {
        return ApiResponse.ok(qslShareService.revoke());
    }
}
