package com.flower_details.features.users.infrastructure.persistence.mapper;

import com.flower_details.features.users.domain.model.Person;
import com.flower_details.features.users.infrastructure.persistence.entity.PersonJpaEntity;
import com.flower_details.features.users.infrastructure.persistence.entity.UserJpaEntity;

public final class PersonPersistenceMapper {

	private PersonPersistenceMapper() {
	}

	public static Person toDomain(PersonJpaEntity entity) {
		return Person.restore(
				entity.getId(),
				entity.getUserId(),
				entity.getNames(),
				entity.getLastnames(),
				entity.getPhone(),
				entity.getDocumentNumber(),
				entity.getCreatedAt(),
				entity.getUpdatedAt()
		);
	}

	public static PersonJpaEntity toEntity(Person person, UserJpaEntity user) {
		return new PersonJpaEntity(
				person.id(),
				user,
				person.names(),
				person.lastNames(),
				person.phone(),
				person.documentNumber(),
				person.createdAt(),
				person.updatedAt()
		);
	}
}
