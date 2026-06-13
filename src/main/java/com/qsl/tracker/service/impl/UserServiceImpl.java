package com.qsl.tracker.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qsl.tracker.common.BusinessException;
import com.qsl.tracker.domain.User;
import com.qsl.tracker.dto.QslShareSummaryResponse;
import com.qsl.tracker.dto.UserProfileRequest;
import com.qsl.tracker.dto.UserProfileResponse;
import com.qsl.tracker.mapper.UserMapper;
import com.qsl.tracker.service.QslShareService;
import com.qsl.tracker.service.UserService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final QslShareService qslShareService;

    @Override
    public UserProfileResponse currentProfile() {
        return toResponse(currentUser());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserProfileResponse updateCurrentProfile(UserProfileRequest request) {
        User user = currentUser();
        user.setCallSign(trimToNull(request.getCallSign(), true));
        user.setEmail(trimToNull(request.getEmail(), false));
        user.setMailingAddress(trimToNull(request.getMailingAddress(), false));
        user.setPhone(trimToNull(request.getPhone(), false));
        user.setRecipient(trimToNull(request.getRecipient(), false));
        user.setPostalCode(trimToNull(request.getPostalCode(), false));
        updateById(user);
        return toResponse(user);
    }

    @Override
    public QslShareSummaryResponse currentQslShare() {
        return qslShareService.current();
    }

    private User currentUser() {
        User user = getById(StpUtil.getLoginIdAsLong());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    private UserProfileResponse toResponse(User user) {
        UserProfileResponse response = new UserProfileResponse();
        BeanUtils.copyProperties(user, response);
        Object previousLoginAt = StpUtil.getSession().get("previousLoginAt");
        if (previousLoginAt instanceof LocalDateTime value) {
            response.setLastLoginAt(value);
        }
        response.setQslShare(qslShareService.current());
        return response;
    }

    private String trimToNull(String value, boolean uppercase) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String result = value.trim();
        return uppercase ? result.toUpperCase() : result;
    }
}
