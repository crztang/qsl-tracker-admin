package com.qsl.tracker.config;

import com.qsl.tracker.common.PasswordUtil;
import com.qsl.tracker.domain.User;
import com.qsl.tracker.mapper.RbacMapper;
import com.qsl.tracker.service.UserService;
import java.util.List;
import java.util.UUID;
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
        if (!initEnabled) {
            return;
        }
        if (userService.count() > 0) {
            migrateLegacyPasswords();
            return;
        }
        User user = new User();
        user.setUsername(username);
        user.setPasswordSalt(null);
        user.setPasswordHash(PasswordUtil.encode(password));
        user.setMustChangePassword(true);
        user.setEnabled(true);
        user.setFailedLoginCount(0);
        userService.save(user);
        rbacMapper.assignRole(user.getId(), "SUPER_ADMIN");
    }

    private void migrateLegacyPasswords() {
        List<User> legacyUsers = userService.list().stream()
                .filter(user -> PasswordUtil.isLegacyHash(user.getPasswordHash()))
                .toList();
        for (User user : legacyUsers) {
            if (username.equals(user.getUsername())) {
                user.setPasswordHash(PasswordUtil.encode(password));
                user.setPasswordSalt(null);
                user.setMustChangePassword(true);
                user.setEnabled(true);
            } else {
                user.setPasswordHash(PasswordUtil.encode(UUID.randomUUID().toString()));
                user.setPasswordSalt(null);
                user.setMustChangePassword(false);
                user.setEnabled(false);
            }
            userService.updateById(user);
        }
    }
}
