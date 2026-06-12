package com.qsl.tracker.service.impl;

import com.qsl.tracker.common.CurrentUserContext;
import com.qsl.tracker.mapper.RbacMapper;
import com.qsl.tracker.service.DataScopeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DataScopeServiceImpl implements DataScopeService {

    private final CurrentUserContext currentUserContext;
    private final RbacMapper rbacMapper;

    @Override
    public boolean canAccessAll() {
        return rbacMapper.countAllDataScope(currentUserContext.userId()) > 0;
    }
}
