package com.flower_details.features.category.presentation.controller;

import com.flower_details.features.category.application.service.CategoryApplicationService;
import com.flower_details.features.category.presentation.dto.request.CreateCategoryRequest;
import com.flower_details.features.category.presentation.dto.request.UpdateCategoryRequest;
import com.flower_details.features.category.presentation.dto.response.CategoryResponse;
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

	private final CategoryApplicationService categoryApplicationService;

	@GetMapping
	List<CategoryResponse> listCategories() {
		return categoryApplicationService.listActiveCategories()
				.stream()
				.map(CategoryResponse::from)
				.toList();
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
		CategoryResponse response = CategoryResponse.from(categoryApplicationService.createCategory(request.toCommand()));
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	CategoryResponse updateCategory(
			@PathVariable Long id,
			@Valid @RequestBody UpdateCategoryRequest request
	) {
		return CategoryResponse.from(categoryApplicationService.updateCategory(request.toCommand(id)));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
		categoryApplicationService.deleteCategory(id);
		return ResponseEntity.noContent().build();
	}
}
