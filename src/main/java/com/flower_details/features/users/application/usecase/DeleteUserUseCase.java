package com.flower_details.features.users.application.usecase;

import com.flower_details.features.users.application.exception.UserNotFoundException;
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
public class DeleteUserUseCase {

	private final UserRepository userRepository;
	private final PersonRepository personRepository;

	@Transactional
	public void execute(Long userId, Long requestedByUserId) {
		if (Objects.equals(userId, requestedByUserId)) {
			throw new DomainException("No puedes eliminar tu propio usuario");
		}

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new UserNotFoundException(userId));
		personRepository.findByUserId(userId).ifPresent(personRepository::delete);
		userRepository.delete(user);
	}
}
