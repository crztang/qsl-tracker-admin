package com.qsl.tracker.controller;

import com.qsl.tracker.service.QslShareService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/public/embed")
public class PublicEmbedController {

    private final QslShareService qslShareService;

    @GetMapping("/{token}")
    public ResponseEntity<String> render(@PathVariable String token, HttpServletRequest request) {
        String html = qslShareService.renderHtml(token, request);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/html; charset=UTF-8"))
                .cacheControl(CacheControl.noStore())
                .header("Content-Security-Policy", "default-src 'none'; style-src 'unsafe-inline'; base-uri 'none'; frame-ancestors *")
                .header("X-Content-Type-Options", "nosniff")
                .body(html);
    }
}
