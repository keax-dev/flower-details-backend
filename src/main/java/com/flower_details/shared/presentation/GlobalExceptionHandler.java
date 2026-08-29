package com.flower_details.shared.presentation;

import com.flower_details.features.auth.application.exception.InvalidCredentialsException;
import com.flower_details.features.auth.application.exception.UserInactiveException;
import com.flower_details.features.category.application.exception.CategoryNotFoundException;
import com.flower_details.features.category.application.exception.CategoryHasProductsException;
import com.flower_details.features.category.application.exception.CategoryTitleAlreadyExistsException;
import com.flower_details.features.cart.application.exception.CartItemNotFoundException;
import com.flower_details.features.cart.application.exception.CartNotFoundException;
import com.flower_details.features.cart.application.exception.CartProductUnavailableException;
import com.flower_details.features.product.application.exception.FileStorageException;
import com.flower_details.features.product.application.exception.ProductImageNotFoundException;
import com.flower_details.features.product.application.exception.ProductNotFoundException;
import com.flower_details.features.order.application.exception.OrderNotFoundException;
import com.flower_details.features.users.application.exception.EmailAlreadyRegisteredException;
import com.flower_details.features.users.application.exception.UserNotFoundException;
import com.flower_details.shared.domain.DomainException;
import com.flower_details.shared.infrastructure.observability.RequestIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ApiErrorResponse> handleValidation(
			MethodArgumentNotValidException exception,
			HttpServletRequest request
	) {
		Map<String, String> errors = new LinkedHashMap<>();
		for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
			errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
		}
		return build(HttpStatus.BAD_REQUEST, "Solicitud invalida", "Revisa los datos enviados", request, errors);
	}

	@ExceptionHandler(EmailAlreadyRegisteredException.class)
	ResponseEntity<ApiErrorResponse> handleEmailAlreadyRegistered(
			EmailAlreadyRegisteredException exception,
			HttpServletRequest request
	) {
		return build(HttpStatus.CONFLICT, "Correo duplicado", exception.getMessage(), request);
	}

	@ExceptionHandler(CategoryTitleAlreadyExistsException.class)
	ResponseEntity<ApiErrorResponse> handleCategoryTitleAlreadyExists(
			CategoryTitleAlreadyExistsException exception,
			HttpServletRequest request
	) {
		return build(HttpStatus.CONFLICT, "Categoria duplicada", exception.getMessage(), request);
	}

	@ExceptionHandler(InvalidCredentialsException.class)
	ResponseEntity<ApiErrorResponse> handleInvalidCredentials(
			InvalidCredentialsException exception,
			HttpServletRequest request
	) {
		return build(HttpStatus.UNAUTHORIZED, "Credenciales invalidas", exception.getMessage(), request);
	}

	@ExceptionHandler(UserInactiveException.class)
	ResponseEntity<ApiErrorResponse> handleUserInactive(UserInactiveException exception, HttpServletRequest request) {
		return build(HttpStatus.FORBIDDEN, "Usuario inactivo", exception.getMessage(), request);
	}

	@ExceptionHandler(UserNotFoundException.class)
	ResponseEntity<ApiErrorResponse> handleUserNotFound(UserNotFoundException exception, HttpServletRequest request) {
		return build(HttpStatus.NOT_FOUND, "Usuario no encontrado", exception.getMessage(), request);
	}

	@ExceptionHandler(CategoryNotFoundException.class)
	ResponseEntity<ApiErrorResponse> handleCategoryNotFound(
			CategoryNotFoundException exception,
			HttpServletRequest request
	) {
		return build(HttpStatus.NOT_FOUND, "Categoria no encontrada", exception.getMessage(), request);
	}

	@ExceptionHandler(CategoryHasProductsException.class)
	ResponseEntity<ApiErrorResponse> handleCategoryHasProducts(
			CategoryHasProductsException exception,
			HttpServletRequest request
	) {
		return build(HttpStatus.CONFLICT, "Categoria con productos", exception.getMessage(), request);
	}

	@ExceptionHandler(ProductNotFoundException.class)
	ResponseEntity<ApiErrorResponse> handleProductNotFound(
			ProductNotFoundException exception,
			HttpServletRequest request
	) {
		return build(HttpStatus.NOT_FOUND, "Producto no encontrado", exception.getMessage(), request);
	}

	@ExceptionHandler(OrderNotFoundException.class)
	ResponseEntity<ApiErrorResponse> handleOrderNotFound(OrderNotFoundException exception, HttpServletRequest request) {
		return build(HttpStatus.NOT_FOUND, "Pedido no encontrado", exception.getMessage(), request);
	}

	@ExceptionHandler({CartNotFoundException.class, CartItemNotFoundException.class, CartProductUnavailableException.class})
	ResponseEntity<ApiErrorResponse> handleCartNotFound(RuntimeException exception, HttpServletRequest request) {
		return build(HttpStatus.NOT_FOUND, "Recurso del carrito no encontrado", exception.getMessage(), request);
	}

	@ExceptionHandler(ProductImageNotFoundException.class)
	ResponseEntity<ApiErrorResponse> handleProductImageNotFound(
			ProductImageNotFoundException exception,
			HttpServletRequest request
	) {
		return build(HttpStatus.NOT_FOUND, "Imagen no encontrada", exception.getMessage(), request);
	}

	@ExceptionHandler(AccessDeniedException.class)
	ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException exception, HttpServletRequest request) {
		return build(HttpStatus.FORBIDDEN, "Acceso denegado", "No tienes permisos para esta accion", request);
	}

	@ExceptionHandler(FileStorageException.class)
	ResponseEntity<ApiErrorResponse> handleFileStorage(FileStorageException exception, HttpServletRequest request) {
		return build(HttpStatus.BAD_REQUEST, "Imagen invalida", exception.getMessage(), request);
	}

	@ExceptionHandler(DomainException.class)
	ResponseEntity<ApiErrorResponse> handleDomain(DomainException exception, HttpServletRequest request) {
		return build(HttpStatus.BAD_REQUEST, "Regla de negocio invalida", exception.getMessage(), request);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	ResponseEntity<ApiErrorResponse> handleDataIntegrity(
			DataIntegrityViolationException exception,
			HttpServletRequest request
	) {
		return build(HttpStatus.CONFLICT, "Conflicto de datos", "No se pudo completar la operacion", request);
	}

	@ExceptionHandler(ObjectOptimisticLockingFailureException.class)
	ResponseEntity<ApiErrorResponse> handleOptimisticLock(
			ObjectOptimisticLockingFailureException exception,
			HttpServletRequest request
	) {
		return build(
				HttpStatus.CONFLICT,
				"El pedido fue actualizado por otro usuario",
				"Actualiza la informacion e intenta nuevamente",
				request
		);
	}

	@ExceptionHandler(Exception.class)
	ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
		return build(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno", "Ocurrio un error inesperado", request);
	}

	private static ResponseEntity<ApiErrorResponse> build(
			HttpStatus status,
			String error,
			String message,
			HttpServletRequest request
	) {
		return ResponseEntity.status(status)
				.body(ApiErrorResponse.of(status.value(), error, message, request.getRequestURI(), requestId(request)));
	}

	private static ResponseEntity<ApiErrorResponse> build(
			HttpStatus status,
			String error,
			String message,
			HttpServletRequest request,
			Map<String, String> validationErrors
	) {
		return ResponseEntity.status(status)
				.body(ApiErrorResponse.withValidationErrors(
						status.value(),
						error,
						message,
						request.getRequestURI(),
						requestId(request),
						validationErrors
				));
	}

	private static String requestId(HttpServletRequest request) {
		Object requestId = request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE);
		return requestId instanceof String value ? value : null;
	}
}
