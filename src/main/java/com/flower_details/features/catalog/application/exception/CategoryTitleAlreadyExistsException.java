package com.flower_details.features.catalog.application.exception;

public class CategoryTitleAlreadyExistsException extends RuntimeException {

	public CategoryTitleAlreadyExistsException(String title) {
		super("Ya existe una categoria con el titulo " + title);
	}
}
