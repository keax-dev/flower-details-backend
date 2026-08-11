package com.flower_details.features.users.application.port.in;

import com.flower_details.features.users.application.dto.UserProfile;

public interface GetUserProfileUseCase {

	UserProfile getById(Long id);
}
