package com.flower_details.features.users.application.service;

import com.flower_details.features.users.application.dto.CreateOperatorCommand;
import com.flower_details.features.users.application.dto.UserProfile;
import com.flower_details.features.users.application.exception.EmailAlreadyRegisteredException;
import com.flower_details.features.users.application.exception.UserNotFoundException;
import com.flower_details.features.users.application.port.in.CreateOperatorUseCase;
import com.flower_details.features.users.application.port.in.DeleteUserUseCase;
import com.flower_details.features.users.application.port.in.GetUserProfileUseCase;
import com.flower_details.features.users.application.port.in.ListUsersUseCase;
import com.flower_details.features.users.application.port.out.PersonRepositoryPort;
import com.flower_details.features.users.application.port.out.UserRepositoryPort;
import com.flower_details.features.users.domain.model.Person;
import com.flower_details.features.users.domain.model.User;
import com.flower_details.features.users.domain.model.UserRole;
import com.flower_details.shared.domain.DomainException;
import com.flower_details.shared.security.PasswordHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserApplicationService implements
		CreateOperatorUseCase,
		DeleteUserUseCase,
		GetUserProfileUseCase,
		ListUsersUseCase {

	private final UserRepositoryPort userRepository;
	private final PersonRepositoryPort personRepository;
	private final PasswordHasher passwordHasher;

	@Override
	@Transactional
	public UserProfile createOperator(CreateOperatorCommand command) {
		if (userRepository.existsByEmail(command.email())) {
			throw new EmailAlreadyRegisteredException(command.email());
		}

		User operator = userRepository.save(User.createStaff(
				command.email(),
				passwordHasher.hash(command.password()),
				UserRole.OPERATOR
		));
		Person person = personRepository.save(Person.create(
				operator.id(),
				command.names(),
				command.lastNames(),
				command.phone(),
				command.documentNumber()
		));

		return UserProfile.from(operator, person);
	}

	@Override
	@Transactional(readOnly = true)
	public UserProfile getById(Long id) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new UserNotFoundException(id));
		return UserProfile.from(user, findPersonByUserId(id));
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserProfile> listUsers() {
		List<User> users = userRepository.findAll();
		Map<Long, Person> peopleByUserId = personRepository.findByUserIds(users.stream()
						.map(User::id)
						.toList())
				.stream()
				.collect(Collectors.toMap(Person::userId, Function.identity()));

		return users.stream()
				.map(user -> UserProfile.from(user, findPersonInMap(user, peopleByUserId)))
				.toList();
	}

	@Override
	@Transactional
	public void deleteUser(Long userId, Long requestedByUserId) {
		if (Objects.equals(userId, requestedByUserId)) {
			throw new DomainException("No puedes eliminar tu propio usuario");
		}

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new UserNotFoundException(userId));

		personRepository.findByUserId(userId).ifPresent(personRepository::delete);
		userRepository.delete(user);
	}

	private Person findPersonByUserId(Long userId) {
		return personRepository.findByUserId(userId)
				.orElseThrow(() -> new UserNotFoundException(userId));
	}

	private Person findPersonInMap(User user, Map<Long, Person> peopleByUserId) {
		Person person = peopleByUserId.get(user.id());
		if (person == null) {
			throw new UserNotFoundException(user.id());
		}
		return person;
	}
}
