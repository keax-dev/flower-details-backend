package com.flower_details.features.users.infrastructure.persistence.mapper;

import com.flower_details.features.users.domain.model.User;
import com.flower_details.features.users.infrastructure.persistence.entity.UserJpaEntity;

public final class UserPersistenceMapper {

	private UserPersistenceMapper() {
	}

	public static User toDomain(UserJpaEntity entity) {
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

	public static UserJpaEntity toEntity(User user) {
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
