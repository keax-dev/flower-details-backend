package com.flower_details.shared.presentation;

import java.time.Instant;
import java.util.Map;

public record ApiErrorResponse(
		Instant timestamp,
		int status,
		String error,
		String message,
		String path,
		String requestId,
		Map<String, String> validationErrors
) {

	public static ApiErrorResponse of(int status, String error, String message, String path, String requestId) {
		return new ApiErrorResponse(Instant.now(), status, error, message, path, requestId, Map.of());
	}

	public static ApiErrorResponse withValidationErrors(
			int status,
			String error,
			String message,
			String path,
			String requestId,
			Map<String, String> validationErrors
	) {
		return new ApiErrorResponse(Instant.now(), status, error, message, path, requestId, validationErrors);
	}
}
