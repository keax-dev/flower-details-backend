package com.flower_details.features.users.application.usecase;

import com.flower_details.features.users.application.dto.command.CreateOperatorCommand;
import com.flower_details.features.users.application.dto.view.UserProfile;
import com.flower_details.features.users.application.exception.EmailAlreadyRegisteredException;
import com.flower_details.features.users.domain.model.Person;
import com.flower_details.features.users.domain.model.User;
import com.flower_details.features.users.domain.model.UserRole;
import com.flower_details.features.users.domain.repository.PersonRepository;
import com.flower_details.features.users.domain.repository.UserRepository;
import com.flower_details.shared.domain.security.PasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateOperatorUseCase {

	private final UserRepository userRepository;
	private final PersonRepository personRepository;
	private final PasswordService passwordService;

	@Transactional
	public UserProfile execute(CreateOperatorCommand command) {
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
}
