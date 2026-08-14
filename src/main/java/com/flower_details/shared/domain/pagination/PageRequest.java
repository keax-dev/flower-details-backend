package com.flower_details.shared.domain.pagination;

public record PageRequest(int page, int size) {

	public PageRequest {
		if (page < 0) {
			throw new IllegalArgumentException("La pagina no puede ser negativa");
		}
		if (size < 1 || size > 100) {
			throw new IllegalArgumentException("El tamano de pagina debe estar entre 1 y 100");
		}
	}
}
