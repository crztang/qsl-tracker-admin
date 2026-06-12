package com.qsl.tracker.config;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import com.qsl.tracker.common.ApiResponse;
import com.qsl.tracker.common.BusinessException;
import com.qsl.tracker.common.RequestLogContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final RequestLogContext requestLogContext;

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("Business exception: {}, code={}, message={}",
                requestLogContext.describe(request), e.getCode(), e.getMessage());
        return ApiResponse.fail(e.getCode(), e.getMessage());
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(NotLoginException.class)
    public ApiResponse<Void> handleNotLoginException(NotLoginException e, HttpServletRequest request) {
        log.warn("Unauthenticated request: {}, message={}",
                requestLogContext.describe(request), e.getMessage());
        return ApiResponse.fail(401, "请先登录");
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(NotPermissionException.class)
    public ApiResponse<Void> handleNotPermissionException(NotPermissionException e, HttpServletRequest request) {
        log.warn("Permission denied: {}, permission={}",
                requestLogContext.describe(request), e.getPermission());
        return ApiResponse.fail(403, "无权执行此操作");
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class, ConstraintViolationException.class})
    public ApiResponse<Void> handleValidationException(Exception e, HttpServletRequest request) {
        log.warn("Validation failed: {}, detail={}",
                requestLogContext.describe(request), e.getMessage());
        return ApiResponse.fail(400, "请求参数不正确");
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("Unhandled exception: {}", requestLogContext.describe(request), e);
        return ApiResponse.fail(500, "系统繁忙，请稍后再试");
    }
}
