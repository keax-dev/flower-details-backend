package com.flower_details.features.category.presentation.controller;

import com.flower_details.features.category.application.usecase.CreateCategoryUseCase;
import com.flower_details.features.category.application.usecase.DeleteCategoryUseCase;
import com.flower_details.features.category.application.usecase.RetrieveCategoriesUseCase;
import com.flower_details.features.category.application.usecase.UpdateCategoryUseCase;
import com.flower_details.features.category.presentation.dto.request.CreateCategoryRequest;
import com.flower_details.features.category.presentation.dto.request.UpdateCategoryRequest;
import com.flower_details.features.category.presentation.dto.response.CategoryResponse;
import com.flower_details.shared.domain.pagination.PageRequest;
import com.flower_details.shared.presentation.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;


@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
class CategoryController {

	private final CreateCategoryUseCase createCategoryUseCase;
	private final RetrieveCategoriesUseCase retrieveCategoriesUseCase;
	private final UpdateCategoryUseCase updateCategoryUseCase;
	private final DeleteCategoryUseCase deleteCategoryUseCase;

	@GetMapping
	PageResponse<CategoryResponse> listCategories(
			@RequestParam(defaultValue = "0") @PositiveOrZero(message = "La pagina no puede ser negativa") int page,
			@RequestParam(defaultValue = "20") @Positive(message = "El tamano debe ser mayor a cero") @Max(value = 100, message = "El tamano maximo es 100") int size
	) {
		return PageResponse.from(
				retrieveCategoriesUseCase.execute(new PageRequest(page, size)),
				CategoryResponse::from
		);
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.CREATED)
	CategoryResponse createCategory(@Valid @RequestBody CreateCategoryRequest request) {
		return CategoryResponse.from(createCategoryUseCase.execute(request.toCommand()));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	CategoryResponse updateCategory(
			@PathVariable Long id,
			@Valid @RequestBody UpdateCategoryRequest request
	) {
		return CategoryResponse.from(updateCategoryUseCase.execute(request.toCommand(id)));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void deleteCategory(@PathVariable Long id) {
		deleteCategoryUseCase.execute(id);
	}
}
