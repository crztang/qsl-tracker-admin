package com.qsl.tracker.controller;

import com.qsl.tracker.common.ApiResponse;
import com.qsl.tracker.common.WebUtil;
import com.qsl.tracker.dto.QslConfirmRequest;
import com.qsl.tracker.dto.QslPublicInfoResponse;
import com.qsl.tracker.service.QslCardService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public/qsl-cards")
public class PublicQslController {

    private final QslCardService qslCardService;

    @GetMapping("/{trackingNo}")
    public ApiResponse<QslPublicInfoResponse> info(@PathVariable String trackingNo) {
        return ApiResponse.ok(qslCardService.publicInfo(trackingNo));
    }

    @PostMapping("/{trackingNo}/confirm")
    public ApiResponse<Void> confirm(
            @PathVariable String trackingNo,
            @Valid @RequestBody QslConfirmRequest request,
            HttpServletRequest httpRequest) {
        qslCardService.confirm(trackingNo, request.getToken(), WebUtil.clientIp(httpRequest), httpRequest.getHeader("User-Agent"));
        return ApiResponse.ok();
    }
}
