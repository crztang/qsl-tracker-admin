package com.qsl.tracker.config;

import com.qsl.tracker.common.PasswordUtil;
import com.qsl.tracker.domain.AdminUser;
import com.qsl.tracker.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final AdminUserService adminUserService;

    @Value("${qsl.admin.init-enabled:true}")
    private boolean initEnabled;
    @Value("${qsl.admin.username:admin}")
    private String username;
    @Value("${qsl.admin.password:admin123}")
    private String password;

    @Override
    public void run(String... args) {
        if (!initEnabled || adminUserService.count() > 0) {
            return;
        }
        String salt = PasswordUtil.newSalt();
        AdminUser user = new AdminUser();
        user.setUsername(username);
        user.setPasswordSalt(salt);
        user.setPasswordHash(PasswordUtil.md5WithSalt(password, salt));
        user.setEnabled(true);
        user.setFailedLoginCount(0);
        adminUserService.save(user);
    }
}
