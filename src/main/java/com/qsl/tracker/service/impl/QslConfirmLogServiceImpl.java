package com.qsl.tracker.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qsl.tracker.domain.QslConfirmLog;
import com.qsl.tracker.mapper.QslConfirmLogMapper;
import com.qsl.tracker.service.QslConfirmLogService;
import org.springframework.stereotype.Service;

@Service
public class QslConfirmLogServiceImpl extends ServiceImpl<QslConfirmLogMapper, QslConfirmLog> implements QslConfirmLogService {
}
