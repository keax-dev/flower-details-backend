package com.flower_details.features.cart.presentation.controller;

import com.flower_details.features.auth.application.security.AuthenticatedUser;
import com.flower_details.features.cart.application.service.CartApplicationService;
import com.flower_details.features.cart.presentation.dto.request.AddCartItemRequest;
import com.flower_details.features.cart.presentation.dto.request.UpdateCartItemRequest;
import com.flower_details.features.cart.presentation.dto.response.CartResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@PreAuthorize("hasRole('CUSTOMER')")
@RequiredArgsConstructor
class CartController {

	private final CartApplicationService cartApplicationService;

	@GetMapping
	CartResponse getCart(@AuthenticationPrincipal AuthenticatedUser principal) {
		return CartResponse.from(cartApplicationService.getCart(principal.id()));
	}

	@PostMapping("/items")
	@ResponseStatus(HttpStatus.CREATED)
	CartResponse addItem(
			@AuthenticationPrincipal AuthenticatedUser principal,
			@Valid @RequestBody AddCartItemRequest request
	) {
		return CartResponse.from(cartApplicationService.addItem(principal.id(), request.toCommand()));
	}

	@PutMapping("/items/{itemId}")
	CartResponse updateItem(
			@AuthenticationPrincipal AuthenticatedUser principal,
			@PathVariable Long itemId,
			@Valid @RequestBody UpdateCartItemRequest request
	) {
		return CartResponse.from(cartApplicationService.updateItem(principal.id(), request.toCommand(itemId)));
	}

	@DeleteMapping("/items/{itemId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void deleteItem(@AuthenticationPrincipal AuthenticatedUser principal, @PathVariable Long itemId) {
		cartApplicationService.deleteItem(principal.id(), itemId);
	}

	@DeleteMapping("/items")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void clearCart(@AuthenticationPrincipal AuthenticatedUser principal) {
		cartApplicationService.clearCart(principal.id());
	}
}
