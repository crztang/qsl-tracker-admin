package com.qsl.tracker.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qsl.tracker.domain.AdminLoginLog;
import com.qsl.tracker.mapper.AdminLoginLogMapper;
import com.qsl.tracker.service.AdminLoginLogService;
import org.springframework.stereotype.Service;

@Service
public class AdminLoginLogServiceImpl extends ServiceImpl<AdminLoginLogMapper, AdminLoginLog> implements AdminLoginLogService {
}
