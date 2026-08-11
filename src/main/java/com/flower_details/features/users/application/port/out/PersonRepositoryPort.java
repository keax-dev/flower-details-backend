package com.flower_details.features.users.application.port.out;

import com.flower_details.features.users.domain.model.Person;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PersonRepositoryPort {

	Person save(Person person);

	void delete(Person person);

	Optional<Person> findByUserId(Long userId);

	List<Person> findByUserIds(Collection<Long> userIds);
}
