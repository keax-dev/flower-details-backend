package com.flower_details.features.users.presentation.controller;

import com.flower_details.features.auth.application.security.AuthenticatedUser;
import com.flower_details.features.users.application.usecase.CreateOperatorUseCase;
import com.flower_details.features.users.application.usecase.CreateStaffUseCase;
import com.flower_details.features.users.application.usecase.DeleteUserUseCase;
import com.flower_details.features.users.application.usecase.PatchActivateUserUseCase;
import com.flower_details.features.users.application.usecase.PatchDeactivateUserUseCase;
import com.flower_details.features.users.application.usecase.RetrieveUsersUseCase;
import com.flower_details.features.users.application.usecase.UpdateOperatorUseCase;
import com.flower_details.features.users.presentation.dto.request.CreateOperatorRequest;
import com.flower_details.features.users.presentation.dto.request.CreateStaffRequest;
import com.flower_details.features.users.presentation.dto.request.UpdateOperatorRequest;
import com.flower_details.features.users.presentation.dto.response.UserResponse;
import com.flower_details.shared.domain.pagination.PageRequest;
import com.flower_details.shared.presentation.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;


@RestController
@RequiredArgsConstructor
class UserController {

	private final RetrieveUsersUseCase retrieveUsersUseCase;
	private final CreateOperatorUseCase createOperatorUseCase;
	private final CreateStaffUseCase createStaffUseCase;
	private final UpdateOperatorUseCase updateOperatorUseCase;
	private final PatchActivateUserUseCase patchActivateUserUseCase;
	private final PatchDeactivateUserUseCase patchDeactivateUserUseCase;
	private final DeleteUserUseCase deleteUserUseCase;

	@GetMapping("/api/me")
	UserResponse me(@AuthenticationPrincipal AuthenticatedUser principal) {
		return UserResponse.from(retrieveUsersUseCase.byId(principal.id()));
	}

	@GetMapping("/api/users")
	@PreAuthorize("hasRole('ADMIN')")
	PageResponse<UserResponse> listUsers(
			@RequestParam(defaultValue = "0") @PositiveOrZero(message = "La pagina no puede ser negativa") int page,
			@RequestParam(defaultValue = "20") @Positive(message = "El tamano debe ser mayor a cero") @Max(value = 100, message = "El tamano maximo es 100") int size
	) {
		return PageResponse.from(
				retrieveUsersUseCase.list(new PageRequest(page, size)),
				UserResponse::from
		);
	}

	@GetMapping("/api/users/operators")
	@PreAuthorize("hasRole('ADMIN')")
	PageResponse<UserResponse> listOperators(
			@RequestParam(defaultValue = "0") @PositiveOrZero(message = "La pagina no puede ser negativa") int page,
			@RequestParam(defaultValue = "20") @Positive(message = "El tamano debe ser mayor a cero") @Max(value = 100, message = "El tamano maximo es 100") int size
	) {
		return PageResponse.from(
				retrieveUsersUseCase.listOperators(new PageRequest(page, size)),
				UserResponse::from
		);
	}

	@GetMapping("/api/users/staff")
	@PreAuthorize("hasRole('ADMIN')")
	PageResponse<UserResponse> listStaff(
			@RequestParam(defaultValue = "0") @PositiveOrZero(message = "La pagina no puede ser negativa") int page,
			@RequestParam(defaultValue = "20") @Positive(message = "El tamano debe ser mayor a cero") @Max(value = 100, message = "El tamano maximo es 100") int size
	) {
		return PageResponse.from(
				retrieveUsersUseCase.listStaff(new PageRequest(page, size)),
				UserResponse::from
		);
	}

	@PostMapping("/api/users/operators")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.CREATED)
	UserResponse createOperator(@Valid @RequestBody CreateOperatorRequest request) {
		return UserResponse.from(createOperatorUseCase.execute(request.toCommand()));
	}

	@PostMapping("/api/users/staff")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.CREATED)
	UserResponse createStaff(@Valid @RequestBody CreateStaffRequest request) {
		return UserResponse.from(createStaffUseCase.execute(request.toCommand()));
	}

	@PutMapping("/api/users/operators/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	UserResponse updateOperator(
			@PathVariable Long id,
			@Valid @RequestBody UpdateOperatorRequest request,
			@AuthenticationPrincipal AuthenticatedUser principal
	) {
		return UserResponse.from(updateOperatorUseCase.execute(request.toCommand(id), principal.id()));
	}

	@PutMapping("/api/users/staff/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	UserResponse updateStaff(
			@PathVariable Long id,
			@Valid @RequestBody UpdateOperatorRequest request,
			@AuthenticationPrincipal AuthenticatedUser principal
	) {
		return UserResponse.from(updateOperatorUseCase.execute(request.toCommand(id), principal.id()));
	}

	@PatchMapping("/api/users/{id}/activate")
	@PreAuthorize("hasRole('ADMIN')")
	UserResponse activateUser(
			@PathVariable Long id,
			@AuthenticationPrincipal AuthenticatedUser principal
	) {
		return UserResponse.from(patchActivateUserUseCase.execute(id, principal.id()));
	}

	@PatchMapping("/api/users/{id}/deactivate")
	@PreAuthorize("hasRole('ADMIN')")
	UserResponse deactivateUser(
			@PathVariable Long id,
			@AuthenticationPrincipal AuthenticatedUser principal
	) {
		return UserResponse.from(patchDeactivateUserUseCase.execute(id, principal.id()));
	}

	@DeleteMapping("/api/users/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void deleteUser(
			@PathVariable Long id,
			@AuthenticationPrincipal AuthenticatedUser principal
	) {
		deleteUserUseCase.execute(id, principal.id());
	}
}
