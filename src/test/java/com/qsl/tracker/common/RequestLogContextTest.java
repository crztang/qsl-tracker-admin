package com.qsl.tracker.common;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class RequestLogContextTest {

    private final RequestLogContext requestLogContext = new RequestLogContext(new ObjectMapper());

    @Test
    void describeShowsMaskedJsonForWhitelistedPost() throws Exception {
        MockHttpServletRequest raw = new MockHttpServletRequest("POST", "/api/auth/login");
        raw.setContentType("application/json");
        raw.setContent("{\"username\":\"demo\",\"password\":\"secret\",\"nested\":{\"confirmToken\":\"abc123\"}}".getBytes());
        HttpServletRequest request = new CachedBodyHttpServletRequest(raw);

        String log = requestLogContext.describe(request);

        assertThat(log).contains("method=POST");
        assertThat(log).contains("uri=/api/auth/login");
        assertThat(log).contains("\"password\":\"***\"");
        assertThat(log).contains("\"confirmToken\":\"***\"");
        assertThat(log).doesNotContain("secret");
    }

    @Test
    void describeOmitsBodyForNonWhitelistedPath() throws Exception {
        MockHttpServletRequest raw = new MockHttpServletRequest("POST", "/api/other");
        raw.setContentType("application/json");
        raw.setContent("{\"password\":\"secret\"}".getBytes());
        HttpServletRequest request = new CachedBodyHttpServletRequest(raw);

        String log = requestLogContext.describe(request);

        assertThat(log).contains("body=omitted");
    }
}
