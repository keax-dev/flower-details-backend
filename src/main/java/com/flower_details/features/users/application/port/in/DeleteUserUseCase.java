package com.flower_details.features.users.application.port.in;

public interface DeleteUserUseCase {

	void deleteUser(Long userId, Long requestedByUserId);
}
