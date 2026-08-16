package com.flower_details.features.auth.presentation.controller;

import com.flower_details.features.auth.application.dto.view.AuthResult;
import com.flower_details.features.auth.application.usecase.LoginUseCase;
import com.flower_details.features.auth.application.usecase.RegisterCustomerUseCase;
import com.flower_details.features.auth.infrastructure.security.cookie.AuthCookieManager;
import com.flower_details.features.auth.presentation.dto.request.LoginRequest;
import com.flower_details.features.auth.presentation.dto.request.RegisterCustomerRequest;
import com.flower_details.features.auth.presentation.dto.response.AuthResponse;
import com.flower_details.features.auth.presentation.dto.response.CsrfTokenResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.web.csrf.CsrfToken;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
class AuthController {

	private final RegisterCustomerUseCase registerCustomerUseCase;
	private final LoginUseCase loginUseCase;
	private final AuthCookieManager authCookieManager;

	@GetMapping("/csrf")
	CsrfTokenResponse csrf(CsrfToken csrfToken) {
		return CsrfTokenResponse.from(csrfToken);
	}

	@PostMapping("/register")
	ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterCustomerRequest request) {
		AuthResult result = registerCustomerUseCase.execute(request.toCommand());
		return ResponseEntity.status(HttpStatus.CREATED)
				.header(HttpHeaders.SET_COOKIE, authCookieManager.createAccessTokenCookie(
						result.accessToken(),
						result.expiresInSeconds()
				).toString())
				.body(AuthResponse.from(result));
	}

	@PostMapping("/login")
	ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
		AuthResult result = loginUseCase.execute(request.toCommand());
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
