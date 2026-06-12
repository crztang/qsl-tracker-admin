package com.qsl.tracker.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qsl.tracker.domain.UserLoginLog;
import com.qsl.tracker.mapper.UserLoginLogMapper;
import com.qsl.tracker.service.UserLoginLogService;
import org.springframework.stereotype.Service;

@Service
public class UserLoginLogServiceImpl extends ServiceImpl<UserLoginLogMapper, UserLoginLog> implements UserLoginLogService {
}
