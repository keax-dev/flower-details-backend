package com.flower_details.features.users.application.usecase;

import com.flower_details.features.users.application.dto.view.UserProfile;
import com.flower_details.features.users.application.exception.UserNotFoundException;
import com.flower_details.features.users.domain.model.Person;
import com.flower_details.features.users.domain.model.User;
import com.flower_details.features.users.domain.repository.PersonRepository;
import com.flower_details.features.users.domain.repository.UserRepository;
import com.flower_details.shared.domain.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PatchActivateUserUseCase {

	private final UserRepository userRepository;
	private final PersonRepository personRepository;

	@Transactional
	public UserProfile execute(Long userId, Long requestedByUserId) {
		validateNotOwnUser(userId, requestedByUserId);
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new UserNotFoundException(userId));
		user.activate();

		User updatedUser = userRepository.save(user);
		return UserProfile.from(updatedUser, findPersonByUserId(updatedUser.id()));
	}

	private void validateNotOwnUser(Long userId, Long requestedByUserId) {
		if (Objects.equals(userId, requestedByUserId)) {
			throw new DomainException("No puedes cambiar el estado de tu propio usuario");
		}
	}

	private Person findPersonByUserId(Long userId) {
		return personRepository.findByUserId(userId)
				.orElseThrow(() -> new UserNotFoundException(userId));
	}
}
