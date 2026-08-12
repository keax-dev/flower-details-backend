package com.flower_details.features.users.presentation;

import com.flower_details.features.auth.infrastructure.security.AuthenticatedUserPrincipal;
import com.flower_details.features.users.application.port.in.CreateOperatorUseCase;
import com.flower_details.features.users.application.port.in.DeleteUserUseCase;
import com.flower_details.features.users.application.port.in.GetUserProfileUseCase;
import com.flower_details.features.users.application.port.in.ListUsersUseCase;
import com.flower_details.features.users.presentation.dto.CreateOperatorRequest;
import com.flower_details.features.users.presentation.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
class UserController {

	private final GetUserProfileUseCase getUserProfileUseCase;
	private final ListUsersUseCase listUsersUseCase;
	private final CreateOperatorUseCase createOperatorUseCase;
	private final DeleteUserUseCase deleteUserUseCase;

	@GetMapping("/api/me")
	UserResponse me(@AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
		return UserResponse.from(getUserProfileUseCase.getById(principal.id()));
	}

	@GetMapping("/api/users")
	@PreAuthorize("hasRole('ADMIN')")
	List<UserResponse> listUsers() {
		return listUsersUseCase.listUsers()
				.stream()
				.map(UserResponse::from)
				.toList();
	}

	@PostMapping("/api/users/operators")
	@PreAuthorize("hasRole('ADMIN')")
	ResponseEntity<UserResponse> createOperator(@Valid @RequestBody CreateOperatorRequest request) {
		UserResponse response = UserResponse.from(createOperatorUseCase.createOperator(request.toCommand()));
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@DeleteMapping("/api/users/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	ResponseEntity<Void> deleteUser(
			@PathVariable Long id,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) {
		deleteUserUseCase.deleteUser(id, principal.id());
		return ResponseEntity.noContent().build();
	}
}
