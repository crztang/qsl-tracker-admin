package com.qsl.tracker.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qsl.tracker.domain.User;
import com.qsl.tracker.dto.QslShareSummaryResponse;
import com.qsl.tracker.dto.UserProfileRequest;
import com.qsl.tracker.dto.UserProfileResponse;

public interface UserService extends IService<User> {

    UserProfileResponse currentProfile();

    UserProfileResponse updateCurrentProfile(UserProfileRequest request);

    QslShareSummaryResponse currentQslShare();
}
