package com.flower_details.features.order.presentation.controller;

import com.flower_details.features.auth.infrastructure.security.AuthenticatedUserPrincipal;
import com.flower_details.features.order.application.service.OrderApplicationService;
import com.flower_details.features.order.presentation.dto.request.AssignOrderRequest;
import com.flower_details.features.order.presentation.dto.request.CancelOrderRequest;
import com.flower_details.features.order.presentation.dto.request.ChangeOrderStatusRequest;
import com.flower_details.features.order.presentation.dto.request.CreateOrderRequest;
import com.flower_details.features.order.presentation.dto.response.OrderResponse;
import com.flower_details.features.order.presentation.dto.response.OrderAuditResponse;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
class OrderController {

	private final OrderApplicationService service;

	@PostMapping
	@PreAuthorize("hasRole('CUSTOMER')")
	@ResponseStatus(HttpStatus.CREATED)
	OrderResponse create(
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal,
			@Valid @RequestBody CreateOrderRequest request
	) {
		return OrderResponse.from(service.create(principal.id(), request.toCommand()));
	}

	@GetMapping("/my")
	@PreAuthorize("hasRole('CUSTOMER')")
	PageResponse<OrderResponse> mine(
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal,
			@RequestParam(defaultValue = "0") @PositiveOrZero int page,
			@RequestParam(defaultValue = "20") @Positive @Max(100) int size
	) {
		return PageResponse.from(service.myOrders(principal.id(), new PageRequest(page, size)), OrderResponse::from);
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
	PageResponse<OrderResponse> all(
			@RequestParam(defaultValue = "0") @PositiveOrZero int page,
			@RequestParam(defaultValue = "20") @Positive @Max(100) int size
	) {
		return PageResponse.from(service.allOrders(new PageRequest(page, size)), OrderResponse::from);
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('CUSTOMER','ADMIN','OPERATOR')")
	OrderResponse get(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
		return OrderResponse.from(service.get(id, principal.id(), principal.role()));
	}

	@GetMapping("/{id}/audit")
	@PreAuthorize("hasAnyRole('CUSTOMER','ADMIN','OPERATOR')")
	List<OrderAuditResponse> auditTrail(
			@PathVariable Long id,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) {
		return service.auditTrail(id, principal.id(), principal.role()).stream().map(OrderAuditResponse::from).toList();
	}

	@PatchMapping("/{id}/assign")
	@PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
	OrderResponse assign(
			@PathVariable Long id,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal,
			@Valid @RequestBody AssignOrderRequest request
	) {
		return OrderResponse.from(service.assign(id, principal.id(), principal.role(), request.operatorId()));
	}

	@PatchMapping("/{id}/status")
	@PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
	OrderResponse changeStatus(
			@PathVariable Long id,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal,
			@Valid @RequestBody ChangeOrderStatusRequest request
	) {
		return OrderResponse.from(service.changeStatus(id, principal.id(), principal.role(), request.status()));
	}

	@PatchMapping("/{id}/cancel")
	@PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void cancel(
			@PathVariable Long id,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal,
			@Valid @RequestBody CancelOrderRequest request
	) {
		service.cancel(id, principal.id(), principal.role(), request.reason());
	}
}
