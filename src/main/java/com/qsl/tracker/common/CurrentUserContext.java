package com.qsl.tracker.common;

import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserContext {

    public Long userId() {
        return StpUtil.getLoginIdAsLong();
    }
}
