package com.flower_details.features.auth.application.service;

import com.flower_details.features.auth.application.dto.AuthResult;
import com.flower_details.features.auth.application.dto.LoginCommand;
import com.flower_details.features.auth.application.dto.RegisterCustomerCommand;
import com.flower_details.features.auth.application.exception.InvalidCredentialsException;
import com.flower_details.features.auth.application.exception.UserInactiveException;
import com.flower_details.features.auth.application.port.in.LoginUseCase;
import com.flower_details.features.auth.application.port.in.RegisterCustomerUseCase;
import com.flower_details.features.auth.application.port.out.TokenProviderPort;
import com.flower_details.features.users.application.dto.UserProfile;
import com.flower_details.features.users.application.exception.EmailAlreadyRegisteredException;
import com.flower_details.features.users.application.exception.UserNotFoundException;
import com.flower_details.features.users.application.port.out.PersonRepositoryPort;
import com.flower_details.features.users.application.port.out.UserRepositoryPort;
import com.flower_details.features.users.domain.model.Person;
import com.flower_details.features.users.domain.model.User;
import com.flower_details.shared.security.PasswordHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthApplicationService implements RegisterCustomerUseCase, LoginUseCase {

	private final UserRepositoryPort userRepository;
	private final PersonRepositoryPort personRepository;
	private final PasswordHasher passwordHasher;
	private final TokenProviderPort tokenProvider;

	@Override
	@Transactional
	public AuthResult registerCustomer(RegisterCustomerCommand command) {
		if (userRepository.existsByEmail(command.email())) {
			throw new EmailAlreadyRegisteredException(command.email());
		}

		User customer = userRepository.save(User.registerCustomer(
				command.email(),
				passwordHasher.hash(command.password())
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

	@Override
	@Transactional(readOnly = true)
	public AuthResult login(LoginCommand command) {
		User user = userRepository.findByEmail(command.email())
				.orElseThrow(InvalidCredentialsException::new);

		if (!passwordHasher.matches(command.password(), user.passwordHash())) {
			throw new InvalidCredentialsException();
		}

		if (!user.active()) {
			throw new UserInactiveException();
		}

		return buildAuthResult(user, findPersonByUserId(user.id()));
	}

	private AuthResult buildAuthResult(User user, Person person) {
		return AuthResult.bearer(
				tokenProvider.generate(user),
				tokenProvider.expirationSeconds(),
				UserProfile.from(user, person)
		);
	}

	private Person findPersonByUserId(Long userId) {
		return personRepository.findByUserId(userId)
				.orElseThrow(() -> new UserNotFoundException(userId));
	}
}
