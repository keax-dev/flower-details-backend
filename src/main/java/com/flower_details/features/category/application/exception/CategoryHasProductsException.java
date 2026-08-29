package com.flower_details.features.category.application.exception;

public class CategoryHasProductsException extends RuntimeException {

	public CategoryHasProductsException(Long categoryId) {
		super("No se puede eliminar la categoria porque tiene productos asociados");
	}
}
