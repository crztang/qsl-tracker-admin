package com.qsl.tracker.controller;

import com.qsl.tracker.common.ApiResponse;
import com.qsl.tracker.dto.PrintTemplateRequest;
import com.qsl.tracker.dto.PrintTemplateResponse;
import com.qsl.tracker.service.PrintTemplateService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/print-templates")
public class PrintTemplateController {

    private final PrintTemplateService printTemplateService;

    @GetMapping
    public ApiResponse<List<PrintTemplateResponse>> list(
            @RequestParam(required = false) String templateType) {
        return ApiResponse.ok(printTemplateService.listByUser(templateType));
    }

    @GetMapping("/detail")
    public ApiResponse<PrintTemplateResponse> detail(@RequestParam Long id) {
        return ApiResponse.ok(printTemplateService.detail(id));
    }

    @PostMapping
    public ApiResponse<PrintTemplateResponse> create(
            @Valid @RequestBody PrintTemplateRequest request) {
        return ApiResponse.ok(printTemplateService.create(request));
    }

    @PostMapping("/update")
    public ApiResponse<PrintTemplateResponse> update(
            @Valid @RequestBody PrintTemplateRequest request) {
        return ApiResponse.ok(printTemplateService.update(request));
    }

    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestBody PrintTemplateRequest request) {
        printTemplateService.delete(request.getId());
        return ApiResponse.ok();
    }

    @PostMapping("/default")
    public ApiResponse<Void> setDefault(@RequestBody PrintTemplateRequest request) {
        printTemplateService.setDefault(request.getId());
        return ApiResponse.ok();
    }
}
