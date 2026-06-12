package com.qsl.tracker.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.qsl.tracker.common.ApiResponse;
import com.qsl.tracker.domain.SysDictItem;
import com.qsl.tracker.service.DictService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dicts")
public class DictController {

    private final DictService dictService;

    @GetMapping("/{code}/items")
    @SaCheckLogin
    public ApiResponse<List<SysDictItem>> items(@PathVariable String code) {
        return ApiResponse.ok(dictService.listItems(code));
    }
}
