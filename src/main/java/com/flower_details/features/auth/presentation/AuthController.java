package com.flower_details.features.auth.presentation;

import com.flower_details.features.auth.application.dto.AuthResult;
import com.flower_details.features.auth.application.port.in.LoginUseCase;
import com.flower_details.features.auth.application.port.in.RegisterCustomerUseCase;
import com.flower_details.features.auth.infrastructure.security.AuthCookieManager;
import com.flower_details.features.auth.presentation.dto.AuthResponse;
import com.flower_details.features.auth.presentation.dto.LoginRequest;
import com.flower_details.features.auth.presentation.dto.RegisterCustomerRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
class AuthController {

	private final RegisterCustomerUseCase registerCustomerUseCase;
	private final LoginUseCase loginUseCase;
	private final AuthCookieManager authCookieManager;

	AuthController(
			RegisterCustomerUseCase registerCustomerUseCase,
			LoginUseCase loginUseCase,
			AuthCookieManager authCookieManager
	) {
		this.registerCustomerUseCase = registerCustomerUseCase;
		this.loginUseCase = loginUseCase;
		this.authCookieManager = authCookieManager;
	}

	@PostMapping("/register")
	ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterCustomerRequest request) {
		AuthResult result = registerCustomerUseCase.registerCustomer(request.toCommand());
		return ResponseEntity.status(HttpStatus.CREATED)
				.header(HttpHeaders.SET_COOKIE, authCookieManager.createAccessTokenCookie(
						result.accessToken(),
						result.expiresInSeconds()
				).toString())
				.body(AuthResponse.from(result));
	}

	@PostMapping("/login")
	ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
		AuthResult result = loginUseCase.login(request.toCommand());
		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, authCookieManager.createAccessTokenCookie(
						result.accessToken(),
						result.expiresInSeconds()
				).toString())
				.body(AuthResponse.from(result));
	}

	@PostMapping("/logout")
	ResponseEntity<Void> logout() {
		return ResponseEntity.noContent()
				.header(HttpHeaders.SET_COOKIE, authCookieManager.clearAccessTokenCookie().toString())
				.build();
	}
}
