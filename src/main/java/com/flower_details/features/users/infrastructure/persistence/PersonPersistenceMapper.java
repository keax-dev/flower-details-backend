package com.flower_details.features.users.infrastructure.persistence;

import com.flower_details.features.users.domain.model.Person;

final class PersonPersistenceMapper {

	private PersonPersistenceMapper() {
	}

	static Person toDomain(PersonJpaEntity entity) {
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

	static PersonJpaEntity toEntity(Person person, UserJpaEntity user) {
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
