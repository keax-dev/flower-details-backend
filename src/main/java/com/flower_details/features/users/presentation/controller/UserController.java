package com.flower_details.features.users.presentation.controller;

import com.flower_details.features.auth.infrastructure.security.AuthenticatedUserPrincipal;
import com.flower_details.features.users.application.service.UserApplicationService;
import com.flower_details.features.users.presentation.dto.request.CreateOperatorRequest;
import com.flower_details.features.users.presentation.dto.response.UserResponse;
import com.flower_details.shared.domain.pagination.PageRequest;
import com.flower_details.shared.presentation.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
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
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RestController
@RequiredArgsConstructor
class UserController {

	private final UserApplicationService userApplicationService;

	@GetMapping("/api/me")
	UserResponse me(@AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
		return UserResponse.from(userApplicationService.getById(principal.id()));
	}

	@GetMapping("/api/users")
	@PreAuthorize("hasRole('ADMIN')")
	PageResponse<UserResponse> listUsers(
			@RequestParam(defaultValue = "0") @PositiveOrZero(message = "La pagina no puede ser negativa") int page,
			@RequestParam(defaultValue = "20") @Positive(message = "El tamano debe ser mayor a cero") @Max(value = 100, message = "El tamano maximo es 100") int size
	) {
		return PageResponse.from(
				userApplicationService.listUsers(new PageRequest(page, size)),
				UserResponse::from
		);
	}

	@PostMapping("/api/users/operators")
	@PreAuthorize("hasRole('ADMIN')")
	ResponseEntity<UserResponse> createOperator(@Valid @RequestBody CreateOperatorRequest request) {
		UserResponse response = UserResponse.from(userApplicationService.createOperator(request.toCommand()));
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@DeleteMapping("/api/users/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	ResponseEntity<Void> deleteUser(
			@PathVariable Long id,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) {
		userApplicationService.deleteUser(id, principal.id());
		return ResponseEntity.noContent().build();
	}
}
