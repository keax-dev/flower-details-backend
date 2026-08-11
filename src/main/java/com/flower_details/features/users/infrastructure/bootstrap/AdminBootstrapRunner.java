package com.flower_details.features.users.infrastructure.bootstrap;

import com.flower_details.features.users.application.port.out.UserRepositoryPort;
import com.flower_details.features.users.application.port.out.PersonRepositoryPort;
import com.flower_details.features.users.domain.model.Person;
import com.flower_details.features.users.domain.model.User;
import com.flower_details.features.users.domain.model.UserRole;
import com.flower_details.shared.security.PasswordHasher;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class AdminBootstrapRunner implements ApplicationRunner {

	private final AdminBootstrapProperties properties;
	private final UserRepositoryPort userRepository;
	private final PersonRepositoryPort personRepository;
	private final PasswordHasher passwordHasher;

	AdminBootstrapRunner(
			AdminBootstrapProperties properties,
			UserRepositoryPort userRepository,
			PersonRepositoryPort personRepository,
			PasswordHasher passwordHasher
	) {
		this.properties = properties;
		this.userRepository = userRepository;
		this.personRepository = personRepository;
		this.passwordHasher = passwordHasher;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		if (!properties.enabled() || userRepository.existsByEmail(properties.email())) {
			return;
		}

		User admin = userRepository.save(User.createStaff(
				properties.email(),
				passwordHasher.hash(properties.password()),
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
