package com.qsl.tracker.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;

@Component
public class RequestLogContext {

    private static final int MAX_BODY_BYTES = 8 * 1024;
    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "password", "confirmpassword", "newpassword", "currentpassword",
            "captchacode", "token", "confirmtoken", "authorization", "secret", "pwd", "passwd");
    private static final List<String> BODY_PATTERNS = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/change-password",
            "/api/qso-logs/**",
            "/api/qsl-cards/**",
            "/api/print-templates/**",
            "/api/public/qsl-cards/*/confirm",
            "/api/user-profile/**");

    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public RequestLogContext(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String describe(HttpServletRequest request) {
        StringBuilder builder = new StringBuilder();
        String uri = request.getRequestURI();
        if (uri != null && uri.startsWith("/public/embed/")) {
            uri = "/public/embed/**";
        }
        builder.append("method=").append(request.getMethod())
                .append(", uri=").append(uri)
                .append(", query=").append(StringUtils.hasText(request.getQueryString())
                        ? request.getQueryString() : "-")
                .append(", ip=").append(WebUtil.clientIp(request));

        String body = summarizeBody(request);
        if (body != null) {
            builder.append(", body=").append(body);
        }
        return builder.toString();
    }

    private String summarizeBody(HttpServletRequest request) {
        if (!shouldLogBody(request)) {
            return "omitted";
        }
        if (!(request instanceof CachedBodyHttpServletRequest cachedRequest)) {
            return "omitted";
        }
        byte[] bodyBytes = cachedRequest.getCachedBody();
        if (bodyBytes.length == 0) {
            return "omitted";
        }
        if (bodyBytes.length > MAX_BODY_BYTES) {
            return "omitted";
        }
        String contentType = request.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).contains("application/json")) {
            return "omitted";
        }
        try {
            JsonNode node = objectMapper.readTree(new String(bodyBytes, StandardCharsets.UTF_8));
            maskSensitiveFields(node);
            return objectMapper.writeValueAsString(node);
        } catch (Exception ignored) {
            return "omitted";
        }
    }

    private boolean shouldLogBody(HttpServletRequest request) {
        if (!isBodyMethod(request.getMethod())) {
            return false;
        }
        String contentType = request.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).contains("application/json")) {
            return false;
        }
        String path = request.getRequestURI();
        return BODY_PATTERNS.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private boolean isBodyMethod(String method) {
        return "POST".equalsIgnoreCase(method)
                || "PUT".equalsIgnoreCase(method)
                || "PATCH".equalsIgnoreCase(method)
                || "DELETE".equalsIgnoreCase(method);
    }

    private void maskSensitiveFields(JsonNode node) {
        if (node == null) {
            return;
        }
        if (node.isObject()) {
            var fields = node.fields();
            while (fields.hasNext()) {
                var entry = fields.next();
                JsonNode child = entry.getValue();
                String key = entry.getKey();
                if (isSensitiveKey(key) && child != null && !child.isNull()) {
                    ((ObjectNode) node).put(key, "***");
                } else {
                    maskSensitiveFields(child);
                }
            }
        } else if (node.isArray()) {
            for (JsonNode child : node) {
                maskSensitiveFields(child);
            }
        }
    }

    private boolean isSensitiveKey(String key) {
        String normalized = key.replace("_", "").replace("-", "").toLowerCase(Locale.ROOT);
        return SENSITIVE_KEYS.stream().anyMatch(normalized::contains);
    }
}
