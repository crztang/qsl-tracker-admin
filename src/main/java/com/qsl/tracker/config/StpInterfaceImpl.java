package com.qsl.tracker.config;

import cn.dev33.satoken.stp.StpInterface;
import com.qsl.tracker.mapper.RbacMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final RbacMapper rbacMapper;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return rbacMapper.selectPermissionCodes(Long.valueOf(loginId.toString()));
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return rbacMapper.selectRoleCodes(Long.valueOf(loginId.toString()));
    }
}
