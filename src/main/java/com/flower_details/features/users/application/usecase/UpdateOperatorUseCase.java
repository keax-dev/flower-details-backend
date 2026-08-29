package com.flower_details.features.users.application.usecase;

import com.flower_details.features.users.application.dto.command.UpdateOperatorCommand;
import com.flower_details.features.users.application.dto.view.UserProfile;
import com.flower_details.features.users.application.exception.EmailAlreadyRegisteredException;
import com.flower_details.features.users.application.exception.UserNotFoundException;
import com.flower_details.features.users.domain.model.Person;
import com.flower_details.features.users.domain.model.User;
import com.flower_details.features.users.domain.model.UserRole;
import com.flower_details.features.users.domain.repository.PersonRepository;
import com.flower_details.features.users.domain.repository.UserRepository;
import com.flower_details.shared.domain.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateOperatorUseCase {

	private final UserRepository userRepository;
	private final PersonRepository personRepository;

	@Transactional
	public UserProfile execute(UpdateOperatorCommand command, Long requestedByUserId) {
		User operator = userRepository.findById(command.operatorId())
				.orElseThrow(() -> new UserNotFoundException(command.operatorId()));
		if (!operator.role().isStaff()) {
			throw new DomainException("El usuario indicado no pertenece al personal administrativo");
		}
		if (!command.role().isStaff()) {
			throw new DomainException("El rol debe ser ADMIN u OPERATOR");
		}
		if (operator.id().equals(requestedByUserId) && operator.role() != command.role()) {
			throw new DomainException("No puedes cambiar tu propio rol");
		}
		if (userRepository.existsByEmailForAnotherUser(command.email(), operator.id())) {
			throw new EmailAlreadyRegisteredException(command.email());
		}

		Person person = personRepository.findByUserId(operator.id())
				.orElseThrow(() -> new UserNotFoundException(operator.id()));
		operator.updateEmail(command.email());
		operator.updateRole(command.role());
		operator.updateActive(command.active());
		person.update(command.names(), command.lastNames(), command.phone(), null);

		User updatedOperator = userRepository.save(operator);
		Person updatedPerson = personRepository.save(person);
		return UserProfile.from(updatedOperator, updatedPerson);
	}
}
