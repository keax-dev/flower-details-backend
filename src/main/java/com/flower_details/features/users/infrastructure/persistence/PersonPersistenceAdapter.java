package com.flower_details.features.users.infrastructure.persistence;

import com.flower_details.features.users.application.port.out.PersonRepositoryPort;
import com.flower_details.features.users.domain.model.Person;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
class PersonPersistenceAdapter implements PersonRepositoryPort {

	private final SpringDataPersonJpaRepository repository;
	private final EntityManager entityManager;

	@Override
	public Person save(Person person) {
		UserJpaEntity user = entityManager.getReference(UserJpaEntity.class, person.userId());
		PersonJpaEntity saved = repository.save(PersonPersistenceMapper.toEntity(person, user));
		return PersonPersistenceMapper.toDomain(saved);
	}

	@Override
	public void delete(Person person) {
		repository.findById(person.id()).ifPresent(repository::delete);
	}

	@Override
	public Optional<Person> findByUserId(Long userId) {
		return repository.findByUser_Id(userId).map(PersonPersistenceMapper::toDomain);
	}

	@Override
	public List<Person> findByUserIds(Collection<Long> userIds) {
		if (userIds.isEmpty()) {
			return List.of();
		}
		return repository.findByUser_IdIn(userIds)
				.stream()
				.map(PersonPersistenceMapper::toDomain)
				.toList();
	}
}
