package com.flower_details.features.users.infrastructure.bootstrap;

import com.flower_details.features.users.domain.model.Person;
import com.flower_details.features.users.domain.model.User;
import com.flower_details.features.users.domain.model.UserRole;
import com.flower_details.features.users.domain.repository.PersonRepository;
import com.flower_details.features.users.domain.repository.UserRepository;
import com.flower_details.shared.infrastructure.security.BCryptPasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
class AdminBootstrapRunner implements ApplicationRunner {

	private final AdminBootstrapProperties properties;
	private final UserRepository userRepository;
	private final PersonRepository personRepository;
	private final BCryptPasswordService passwordService;

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		if (!properties.enabled() || userRepository.existsByEmail(properties.email())) {
			return;
		}

		User admin = userRepository.save(User.createStaff(
				properties.email(),
				passwordService.hash(properties.password()),
				UserRole.ADMIN
		));
		Person person = Person.create(
				admin.id(),
				properties.names(),
				properties.lastNames(),
				null,
				null
		);

		personRepository.save(person);
	}
}
