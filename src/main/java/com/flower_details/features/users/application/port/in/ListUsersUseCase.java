package com.flower_details.features.users.application.port.in;

import com.flower_details.features.users.application.dto.UserProfile;

import java.util.List;

public interface ListUsersUseCase {

	List<UserProfile> listUsers();
}
