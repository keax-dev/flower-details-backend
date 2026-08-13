package com.flower_details.features.category.application.exception;

public class CategoryNotFoundException extends RuntimeException {

	public CategoryNotFoundException(Long id) {
		super("No existe la categoria con id " + id);
	}
}
