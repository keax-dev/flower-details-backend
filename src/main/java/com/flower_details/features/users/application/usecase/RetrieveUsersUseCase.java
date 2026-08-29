package com.flower_details.features.users.application.usecase;

import com.flower_details.features.users.application.dto.view.UserProfile;
import com.flower_details.features.users.application.exception.UserNotFoundException;
import com.flower_details.features.users.domain.model.Person;
import com.flower_details.features.users.domain.model.User;
import com.flower_details.features.users.domain.model.UserRole;
import com.flower_details.features.users.domain.repository.PersonRepository;
import com.flower_details.features.users.domain.repository.UserRepository;
import com.flower_details.shared.domain.pagination.PageRequest;
import com.flower_details.shared.domain.pagination.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RetrieveUsersUseCase {

	private final UserRepository userRepository;
	private final PersonRepository personRepository;

	@Transactional(readOnly = true)
	public UserProfile byId(Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new UserNotFoundException(userId));
		return UserProfile.from(user, findPersonByUserId(userId));
	}

	@Transactional(readOnly = true)
	public PageResult<UserProfile> list(PageRequest pageRequest) {
		return profiles(userRepository.findAll(pageRequest));
	}

	@Transactional(readOnly = true)
	public PageResult<UserProfile> listOperators(PageRequest pageRequest) {
		return profiles(userRepository.findAllByRole(UserRole.OPERATOR, pageRequest));
	}

	private PageResult<UserProfile> profiles(PageResult<User> userPage) {
		List<User> users = userPage.items();
		Map<Long, Person> peopleByUserId = personRepository.findByUserIds(users.stream()
				.map(User::id)
				.toList())
				.stream()
				.collect(Collectors.toMap(Person::userId, Function.identity()));

		List<UserProfile> profiles = users.stream()
				.map(user -> UserProfile.from(user, findPersonInMap(user, peopleByUserId)))
				.toList();

		return new PageResult<>(
				profiles,
				userPage.page(),
				userPage.size(),
				userPage.totalElements(),
				userPage.totalPages()
		);
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
