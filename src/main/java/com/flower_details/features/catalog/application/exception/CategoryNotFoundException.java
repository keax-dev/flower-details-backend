package com.flower_details.features.catalog.application.exception;

public class CategoryNotFoundException extends RuntimeException {

	public CategoryNotFoundException(Long id) {
		super("No existe la categoria con id " + id);
	}
}
