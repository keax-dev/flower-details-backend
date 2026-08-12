package com.flower_details.features.catalog.presentation;

import com.flower_details.features.catalog.application.port.in.CreateCategoryUseCase;
import com.flower_details.features.catalog.application.port.in.DeleteCategoryUseCase;
import com.flower_details.features.catalog.application.port.in.ListCategoriesUseCase;
import com.flower_details.features.catalog.application.port.in.UpdateCategoryUseCase;
import com.flower_details.features.catalog.presentation.dto.CategoryResponse;
import com.flower_details.features.catalog.presentation.dto.CreateCategoryRequest;
import com.flower_details.features.catalog.presentation.dto.UpdateCategoryRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
class CategoryController {

	private final ListCategoriesUseCase listCategoriesUseCase;
	private final CreateCategoryUseCase createCategoryUseCase;
	private final UpdateCategoryUseCase updateCategoryUseCase;
	private final DeleteCategoryUseCase deleteCategoryUseCase;

	@GetMapping
	List<CategoryResponse> listCategories() {
		return listCategoriesUseCase.listActiveCategories()
				.stream()
				.map(CategoryResponse::from)
				.toList();
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
		CategoryResponse response = CategoryResponse.from(createCategoryUseCase.createCategory(request.toCommand()));
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	CategoryResponse updateCategory(
			@PathVariable Long id,
			@Valid @RequestBody UpdateCategoryRequest request
	) {
		return CategoryResponse.from(updateCategoryUseCase.updateCategory(request.toCommand(id)));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
		deleteCategoryUseCase.deleteCategory(id);
		return ResponseEntity.noContent().build();
	}
}
