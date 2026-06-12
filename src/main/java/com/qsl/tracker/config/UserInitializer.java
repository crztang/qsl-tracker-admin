package com.qsl.tracker.config;

import com.qsl.tracker.common.PasswordUtil;
import com.qsl.tracker.domain.User;
import com.qsl.tracker.service.UserService;
import com.qsl.tracker.mapper.RbacMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserInitializer implements CommandLineRunner {

    private final UserService userService;
    private final RbacMapper rbacMapper;

    @Value("${qsl.user.init-enabled:true}")
    private boolean initEnabled;
    @Value("${qsl.user.username:admin}")
    private String username;
    @Value("${qsl.user.password:admin123}")
    private String password;

    @Override
    public void run(String... args) {
        if (!initEnabled || userService.count() > 0) {
            return;
        }
        String salt = PasswordUtil.newSalt();
        User user = new User();
        user.setUsername(username);
        user.setPasswordSalt(salt);
        user.setPasswordHash(PasswordUtil.md5WithSalt(password, salt));
        user.setEnabled(true);
        user.setFailedLoginCount(0);
        userService.save(user);
        rbacMapper.assignRole(user.getId(), "SUPER_ADMIN");
    }
}
