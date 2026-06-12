package com.qsl.tracker.controller;

import com.qsl.tracker.common.ApiResponse;
import com.qsl.tracker.common.BusinessException;
import com.qsl.tracker.common.PageResponse;
import com.qsl.tracker.domain.QslCard;
import com.qsl.tracker.dto.QslCardQuery;
import com.qsl.tracker.dto.QslCardRequest;
import com.qsl.tracker.dto.QslCardVO;
import com.qsl.tracker.service.QslCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/qsl-cards")
public class QslCardController {

    private final QslCardService qslCardService;

    @GetMapping
    public ApiResponse<PageResponse<QslCardVO>> page(QslCardQuery query) {
        return ApiResponse.ok(qslCardService.page(query));
    }

    @GetMapping("/detail")
    public ApiResponse<QslCard> detail(@RequestParam Long id) {
        QslCard entity = qslCardService.getById(id);
        if (entity == null) {
            throw new BusinessException("QSL卡片不存在");
        }
        return ApiResponse.ok(entity);
    }

    @PostMapping
    public ApiResponse<QslCard> create(@Valid @RequestBody QslCardRequest request) {
        return ApiResponse.ok(qslCardService.create(request));
    }

    @PostMapping("/update")
    public ApiResponse<QslCard> update(@Valid @RequestBody QslCardRequest request) {
        return ApiResponse.ok(qslCardService.update(request.getId(), request));
    }

    @PostMapping("/delete")
    public ApiResponse<Void> delete( @RequestBody QslCardRequest request) {
        if (!qslCardService.removeById(request.getId())) {
            throw new BusinessException("QSL卡片不存在");
        }
        return ApiResponse.ok();
    }
}
