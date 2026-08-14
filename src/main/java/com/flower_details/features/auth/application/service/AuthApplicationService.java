package com.flower_details.features.auth.application.service;

import com.flower_details.features.auth.application.dto.command.LoginCommand;
import com.flower_details.features.auth.application.dto.command.RegisterCustomerCommand;
import com.flower_details.features.auth.application.dto.view.AuthResult;
import com.flower_details.features.auth.application.exception.InvalidCredentialsException;
import com.flower_details.features.auth.application.exception.UserInactiveException;
import com.flower_details.features.auth.infrastructure.security.JwtTokenService;
import com.flower_details.features.users.application.dto.view.UserProfile;
import com.flower_details.features.users.application.exception.EmailAlreadyRegisteredException;
import com.flower_details.features.users.application.exception.UserNotFoundException;
import com.flower_details.features.users.domain.model.Person;
import com.flower_details.features.users.domain.model.User;
import com.flower_details.features.users.domain.repository.PersonRepository;
import com.flower_details.features.users.domain.repository.UserRepository;
import com.flower_details.shared.infrastructure.security.BCryptPasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthApplicationService {

	private final UserRepository userRepository;
	private final PersonRepository personRepository;
	private final BCryptPasswordService passwordService;
	private final JwtTokenService jwtTokenService;

	@Transactional
	public AuthResult registerCustomer(RegisterCustomerCommand command) {
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

		return buildAuthResult(customer, person);
	}

	@Transactional(readOnly = true)
	public AuthResult login(LoginCommand command) {
		User user = userRepository.findByEmail(command.email())
				.orElseThrow(InvalidCredentialsException::new);

		if (!passwordService.matches(command.password(), user.passwordHash())) {
			throw new InvalidCredentialsException();
		}

		if (!user.active()) {
			throw new UserInactiveException();
		}

		return buildAuthResult(user, findPersonByUserId(user.id()));
	}

	private AuthResult buildAuthResult(User user, Person person) {
		return AuthResult.bearer(
				jwtTokenService.generate(user),
				jwtTokenService.expirationSeconds(),
				UserProfile.from(user, person)
		);
	}

	private Person findPersonByUserId(Long userId) {
		return personRepository.findByUserId(userId)
				.orElseThrow(() -> new UserNotFoundException(userId));
	}
}
