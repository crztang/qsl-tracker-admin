package com.qsl.tracker.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class RequestBodyCachingFilterTest {

    @Test
    void multipartRequestsAreSkipped() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        request.setContentType("multipart/form-data; boundary=abc");

        RequestBodyCachingFilter filter = new RequestBodyCachingFilter();

        assertThat(filter.shouldNotFilter(request)).isTrue();
    }
}
