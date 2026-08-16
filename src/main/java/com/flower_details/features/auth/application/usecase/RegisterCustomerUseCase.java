package com.flower_details.features.auth.application.usecase;

import com.flower_details.features.auth.application.dto.command.RegisterCustomerCommand;
import com.flower_details.features.auth.application.dto.view.AuthResult;
import com.flower_details.features.auth.application.service.AccessTokenService;
import com.flower_details.features.users.application.dto.view.UserProfile;
import com.flower_details.features.users.application.exception.EmailAlreadyRegisteredException;
import com.flower_details.features.users.domain.model.Person;
import com.flower_details.features.users.domain.model.User;
import com.flower_details.features.users.domain.repository.PersonRepository;
import com.flower_details.features.users.domain.repository.UserRepository;
import com.flower_details.shared.domain.security.PasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterCustomerUseCase {

	private final UserRepository userRepository;
	private final PersonRepository personRepository;
	private final PasswordService passwordService;
	private final AccessTokenService accessTokenService;

	@Transactional
	public AuthResult execute(RegisterCustomerCommand command) {
		if (userRepository.existsByEmail(command.email())) {
			throw new EmailAlreadyRegisteredException(command.email());
		}

		User customer = userRepository.save(User.registerCustomer(
				command.email(),
				passwordService.hash(command.password())
		));
		Person person = personRepository.save(Person.create(
				customer.id(),
				command.names(),
				command.lastNames(),
				command.phone(),
				command.documentNumber()
		));
		return AuthResult.bearer(
				accessTokenService.generate(customer),
				accessTokenService.expirationSeconds(),
				UserProfile.from(customer, person)
		);
	}
}
