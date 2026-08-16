package com.flower_details.features.auth.application.usecase;

import com.flower_details.features.auth.application.dto.command.LoginCommand;
import com.flower_details.features.auth.application.dto.view.AuthResult;
import com.flower_details.features.auth.application.exception.InvalidCredentialsException;
import com.flower_details.features.auth.application.exception.UserInactiveException;
import com.flower_details.features.auth.application.service.AccessTokenService;
import com.flower_details.features.users.application.dto.view.UserProfile;
import com.flower_details.features.users.application.exception.UserNotFoundException;
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
public class LoginUseCase {

	private final UserRepository userRepository;
	private final PersonRepository personRepository;
	private final PasswordService passwordService;
	private final AccessTokenService accessTokenService;

	@Transactional(readOnly = true)
	public AuthResult execute(LoginCommand command) {
		User user = userRepository.findByEmail(command.email())
				.orElseThrow(InvalidCredentialsException::new);
		if (!passwordService.matches(command.password(), user.passwordHash())) {
			throw new InvalidCredentialsException();
		}
		if (!user.active()) {
			throw new UserInactiveException();
		}
		Person person = personRepository.findByUserId(user.id())
				.orElseThrow(() -> new UserNotFoundException(user.id()));
		return AuthResult.bearer(
				accessTokenService.generate(user),
				accessTokenService.expirationSeconds(),
				UserProfile.from(user, person)
		);
	}
}
