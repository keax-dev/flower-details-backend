package com.flower_details.features.users.application.usecase;

import com.flower_details.features.users.application.dto.command.CreateStaffCommand;
import com.flower_details.features.users.application.dto.view.UserProfile;
import com.flower_details.features.users.application.exception.EmailAlreadyRegisteredException;
import com.flower_details.features.users.domain.model.Person;
import com.flower_details.features.users.domain.model.User;
import com.flower_details.features.users.domain.repository.PersonRepository;
import com.flower_details.features.users.domain.repository.UserRepository;
import com.flower_details.shared.domain.DomainException;
import com.flower_details.shared.domain.security.PasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateStaffUseCase {

	private final UserRepository userRepository;
	private final PersonRepository personRepository;
	private final PasswordService passwordService;

	@Transactional
	public UserProfile execute(CreateStaffCommand command) {
		if (!command.role().isStaff()) {
			throw new DomainException("Solo se pueden crear usuarios ADMIN u OPERATOR desde este recurso");
		}
		if (userRepository.existsByEmail(command.email())) {
			throw new EmailAlreadyRegisteredException(command.email());
		}

		User user = User.createStaff(command.email(), passwordService.hash(command.password()), command.role());
		user.updateActive(command.active());
		user = userRepository.save(user);
		Person person = personRepository.save(Person.create(
				user.id(), command.names(), command.lastNames(), command.phone(), null
		));
		return UserProfile.from(user, person);
	}
}
