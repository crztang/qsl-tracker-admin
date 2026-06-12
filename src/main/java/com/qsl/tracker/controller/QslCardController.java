package com.qsl.tracker.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.qsl.tracker.common.ApiResponse;
import com.qsl.tracker.common.PageResponse;
import com.qsl.tracker.domain.QslCard;
import com.qsl.tracker.dto.QslCardQuery;
import com.qsl.tracker.dto.QslCardRequest;
import com.qsl.tracker.dto.QslCardVO;
import com.qsl.tracker.service.QslCardService;
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
@RequestMapping("/api/qsl-cards")
public class QslCardController {

    private final QslCardService qslCardService;

    @GetMapping
    @SaCheckPermission("qsl:read")
    public ApiResponse<PageResponse<QslCardVO>> page(QslCardQuery query) {
        return ApiResponse.ok(qslCardService.page(query));
    }

    @GetMapping("/detail")
    @SaCheckPermission("qsl:read")
    public ApiResponse<QslCard> detail(@RequestParam Long id) {
        return ApiResponse.ok(qslCardService.detail(id));
    }

    @PostMapping
    @SaCheckPermission("qsl:write")
    public ApiResponse<QslCard> create(@Valid @RequestBody QslCardRequest request) {
        return ApiResponse.ok(qslCardService.create(request));
    }

    @PostMapping("/update")
    @SaCheckPermission("qsl:write")
    public ApiResponse<QslCard> update(@Valid @RequestBody QslCardRequest request) {
        return ApiResponse.ok(qslCardService.update(request.getId(), request));
    }

    @PostMapping("/delete")
    @SaCheckPermission("qsl:write")
    public ApiResponse<Void> delete(@RequestBody QslCardRequest request) {
        qslCardService.delete(request.getId());
        return ApiResponse.ok();
    }
}
