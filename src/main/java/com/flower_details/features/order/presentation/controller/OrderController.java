package com.flower_details.features.order.presentation.controller;

import com.flower_details.features.auth.infrastructure.security.AuthenticatedUserPrincipal;
import com.flower_details.features.order.application.service.OrderApplicationService;
import com.flower_details.features.order.domain.model.FulfillmentType;
import com.flower_details.features.order.domain.model.OrderSearchCriteria;
import com.flower_details.features.order.domain.model.OrderStatus;
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
import org.springframework.format.annotation.DateTimeFormat;

import java.util.List;
import java.time.LocalDate;

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
			@RequestParam(defaultValue = "20") @Positive @Max(100) int size,
			@RequestParam(required = false) String q,
			@RequestParam(required = false) OrderStatus status,
			@RequestParam(required = false) FulfillmentType fulfillmentType,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdFrom,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdTo,
			@RequestParam(defaultValue = "createdAt") String sortBy,
			@RequestParam(defaultValue = "desc") String direction
	) {
		return PageResponse.from(
				service.myOrders(principal.id(), orderCriteria(q, null, null, status, fulfillmentType, createdFrom, createdTo, sortBy, direction), new PageRequest(page, size)),
				OrderResponse::from
		);
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
	PageResponse<OrderResponse> all(
			@RequestParam(defaultValue = "0") @PositiveOrZero int page,
			@RequestParam(defaultValue = "20") @Positive @Max(100) int size,
			@RequestParam(required = false) String q,
			@RequestParam(required = false) @Positive Long customerId,
			@RequestParam(required = false) @Positive Long operatorId,
			@RequestParam(required = false) OrderStatus status,
			@RequestParam(required = false) FulfillmentType fulfillmentType,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdFrom,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdTo,
			@RequestParam(defaultValue = "createdAt") String sortBy,
			@RequestParam(defaultValue = "desc") String direction
	) {
		return PageResponse.from(
				service.allOrders(orderCriteria(q, customerId, operatorId, status, fulfillmentType, createdFrom, createdTo, sortBy, direction), new PageRequest(page, size)),
				OrderResponse::from
		);
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

	private static OrderSearchCriteria orderCriteria(
			String query,
			Long customerId,
			Long operatorId,
			OrderStatus status,
			FulfillmentType fulfillmentType,
			LocalDate createdFrom,
			LocalDate createdTo,
			String sortBy,
			String direction
	) {
		return OrderSearchCriteria.fromApi(
				query, customerId, operatorId, status, fulfillmentType, createdFrom, createdTo, sortBy, direction
		);
	}
}
