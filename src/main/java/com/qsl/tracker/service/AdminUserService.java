package com.qsl.tracker.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qsl.tracker.domain.AdminUser;
import com.qsl.tracker.dto.UserProfileRequest;
import com.qsl.tracker.dto.UserProfileResponse;

public interface AdminUserService extends IService<AdminUser> {

    UserProfileResponse currentProfile();

    UserProfileResponse updateCurrentProfile(UserProfileRequest request);
}
