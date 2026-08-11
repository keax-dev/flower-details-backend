package com.flower_details.features.users.infrastructure.persistence;

import com.flower_details.features.users.domain.model.User;

final class UserPersistenceMapper {

	private UserPersistenceMapper() {
	}

	static User toDomain(UserJpaEntity entity) {
		return User.restore(
				entity.getId(),
				entity.getEmail(),
				entity.getPasswordHash(),
				entity.getRole(),
				entity.isActive(),
				entity.getCreatedAt(),
				entity.getUpdatedAt()
		);
	}

	static UserJpaEntity toEntity(User user) {
		return new UserJpaEntity(
				user.id(),
				user.email(),
				user.passwordHash(),
				user.role(),
				user.active(),
				user.createdAt(),
				user.updatedAt()
		);
	}
}
