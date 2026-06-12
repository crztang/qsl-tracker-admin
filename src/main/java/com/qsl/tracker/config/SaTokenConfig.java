package com.qsl.tracker.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import com.qsl.tracker.common.BusinessException;
import com.qsl.tracker.domain.User;
import com.qsl.tracker.mapper.UserMapper;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class SaTokenConfig implements WebMvcConfigurer {

    private static final Set<String> PASSWORD_CHANGE_ALLOWED_PATHS = Set.of(
            "/api/auth/change-password",
            "/api/auth/logout",
            "/api/auth/me");

    private final UserMapper userMapper;

    @Bean
    public StpLogic stpLogicJwt() {
        return new StpLogicJwtForSimple();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
                    StpUtil.checkLogin();
                    User user = userMapper.selectById(StpUtil.getLoginIdAsLong());
                    if (user == null || Boolean.FALSE.equals(user.getEnabled())) {
                        StpUtil.logout();
                        throw new BusinessException(403, "账号已停用");
                    }
                    if (Boolean.TRUE.equals(user.getMustChangePassword())
                            && !PASSWORD_CHANGE_ALLOWED_PATHS.contains(SaHolder.getRequest().getRequestPath())) {
                        throw new BusinessException(403, "请先修改初始密码");
                    }
                }))
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/auth/captcha",
                        "/api/public/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html"
                );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
