package com.flower_details.features.users.application.service;

import com.flower_details.features.users.application.dto.command.CreateOperatorCommand;
import com.flower_details.features.users.application.dto.view.UserProfile;
import com.flower_details.features.users.application.exception.EmailAlreadyRegisteredException;
import com.flower_details.features.users.application.exception.UserNotFoundException;
import com.flower_details.features.users.domain.model.Person;
import com.flower_details.features.users.domain.model.User;
import com.flower_details.features.users.domain.model.UserRole;
import com.flower_details.features.users.domain.repository.PersonRepository;
import com.flower_details.features.users.domain.repository.UserRepository;
import com.flower_details.shared.domain.DomainException;
import com.flower_details.shared.domain.security.PasswordService;
import com.flower_details.shared.domain.pagination.PageRequest;
import com.flower_details.shared.domain.pagination.PageResult;
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
public class UserApplicationService {

	private final UserRepository userRepository;
	private final PersonRepository personRepository;
	private final PasswordService passwordService;

	@Transactional
	public UserProfile createOperator(CreateOperatorCommand command) {
		if (userRepository.existsByEmail(command.email())) {
			throw new EmailAlreadyRegisteredException(command.email());
		}

		User operator = userRepository.save(User.createStaff(
				command.email(),
				passwordService.hash(command.password()),
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

	@Transactional(readOnly = true)
	public UserProfile getById(Long id) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new UserNotFoundException(id));
		return UserProfile.from(user, findPersonByUserId(id));
	}

	@Transactional(readOnly = true)
	public PageResult<UserProfile> listUsers(PageRequest pageRequest) {
		PageResult<User> userPage = userRepository.findAll(pageRequest);
		List<User> users = userPage.items();
		Map<Long, Person> peopleByUserId = personRepository.findByUserIds(users.stream()
						.map(User::id)
						.toList())
				.stream()
				.collect(Collectors.toMap(Person::userId, Function.identity()));

		return new PageResult<>(users.stream()
				.map(user -> UserProfile.from(user, findPersonInMap(user, peopleByUserId)))
				.toList(), userPage.page(), userPage.size(), userPage.totalElements(), userPage.totalPages());
	}

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

	@Transactional
	public UserProfile activateUser(Long userId, Long requestedByUserId) {
		return changeActivation(userId, requestedByUserId, true);
	}

	@Transactional
	public UserProfile deactivateUser(Long userId, Long requestedByUserId) {
		return changeActivation(userId, requestedByUserId, false);
	}

	private UserProfile changeActivation(Long userId, Long requestedByUserId, boolean active) {
		if (Objects.equals(userId, requestedByUserId)) {
			throw new DomainException("No puedes cambiar el estado de tu propio usuario");
		}

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new UserNotFoundException(userId));
		if (active) {
			user.activate();
		} else {
			user.deactivate();
		}

		User updatedUser = userRepository.save(user);
		return UserProfile.from(updatedUser, findPersonByUserId(updatedUser.id()));
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
