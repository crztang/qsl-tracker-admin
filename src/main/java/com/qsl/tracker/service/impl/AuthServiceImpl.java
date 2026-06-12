package com.qsl.tracker.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qsl.tracker.common.BusinessException;
import com.qsl.tracker.common.PasswordUtil;
import com.qsl.tracker.common.WebUtil;
import com.qsl.tracker.domain.UserLoginLog;
import com.qsl.tracker.domain.User;
import com.qsl.tracker.dto.LoginRequest;
import com.qsl.tracker.dto.LoginResponse;
import com.qsl.tracker.service.AuthService;
import com.qsl.tracker.service.UserLoginLogService;
import com.qsl.tracker.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final int MAX_FAILED_COUNT = 5;
    private static final int LOCK_MINUTES = 15;

    private final UserService userService;
    private final UserLoginLogService userLoginLogService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String username = request.getUsername().trim();
        String ip = WebUtil.clientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        User user = userService.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .last("limit 1"));
        if (user == null) {
            saveLoginLog(null, username, ip, userAgent, false, "账号不存在");
            throw new BusinessException("账号或密码错误");
        }
        if (Boolean.FALSE.equals(user.getEnabled())) {
            saveLoginLog(user.getId(), username, ip, userAgent, false, "账号已停用");
            throw new BusinessException("账号已停用");
        }
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            saveLoginLog(user.getId(), username, ip, userAgent, false, "账号临时锁定");
            throw new BusinessException("登录失败次数过多，请稍后再试");
        }

        String hash = PasswordUtil.md5WithSalt(request.getPassword(), user.getPasswordSalt());
        if (!hash.equalsIgnoreCase(user.getPasswordHash())) {
            int failedCount = user.getFailedLoginCount() == null ? 1 : user.getFailedLoginCount() + 1;
            user.setFailedLoginCount(failedCount);
            if (failedCount >= MAX_FAILED_COUNT) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_MINUTES));
            }
            userService.updateById(user);
            saveLoginLog(user.getId(), username, ip, userAgent, false, "密码错误");
            throw new BusinessException("账号或密码错误");
        }

        LocalDateTime previousLoginAt = user.getLastLoginAt();
        user.setFailedLoginCount(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(LocalDateTime.now());
        user.setLastLoginIp(ip);
        userService.updateById(user);
        saveLoginLog(user.getId(), username, ip, userAgent, true, null);

        StpUtil.login(user.getId());
        if (previousLoginAt != null) {
            StpUtil.getSession().set("previousLoginAt", previousLoginAt);
        }
        return new LoginResponse(StpUtil.getTokenName(), StpUtil.getTokenValue(), user.getId(), user.getUsername());
    }

    @Override
    public void logout() {
        StpUtil.logout();
    }

    private void saveLoginLog(Long userId, String username, String ip, String userAgent, boolean success, String failReason) {
        UserLoginLog log = new UserLoginLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setLoginIp(ip);
        log.setUserAgent(userAgent);
        log.setSuccess(success);
        log.setFailReason(failReason);
        userLoginLogService.save(log);
    }
}
