package com.flower_details.features.product.presentation.controller;

import com.flower_details.features.product.application.dto.storage.UploadFile;
import com.flower_details.features.product.application.dto.query.ProductSearchQuery;
import com.flower_details.features.product.application.usecase.CreateProductImagesUseCase;
import com.flower_details.features.product.application.usecase.CreateProductUseCase;
import com.flower_details.features.product.application.usecase.DeleteProductUseCase;
import com.flower_details.features.product.application.usecase.RetrieveProductsUseCase;
import com.flower_details.features.product.application.usecase.UpdateProductUseCase;
import com.flower_details.features.product.presentation.dto.request.CreateProductRequest;
import com.flower_details.features.product.presentation.dto.request.UpdateProductRequest;
import com.flower_details.features.product.presentation.dto.response.ProductResponse;
import com.flower_details.features.product.presentation.upload.MultipartUploadFile;
import com.flower_details.shared.domain.pagination.PageRequest;
import com.flower_details.shared.presentation.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.math.BigDecimal;

@Validated
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
class ProductController {

	private final RetrieveProductsUseCase retrieveProductsUseCase;
	private final CreateProductUseCase createProductUseCase;
	private final UpdateProductUseCase updateProductUseCase;
	private final CreateProductImagesUseCase createProductImagesUseCase;
	private final DeleteProductUseCase deleteProductUseCase;

	@GetMapping
	PageResponse<ProductResponse> listProducts(
			@RequestParam(defaultValue = "0") @PositiveOrZero(message = "La pagina no puede ser negativa") int page,
			@RequestParam(defaultValue = "20") @Positive @Max(value = 100, message = "El tamano maximo es 100") int size,
			@RequestParam(required = false) @Size(max = 120) String q,
			@RequestParam(required = false) @Positive Long categoryId,
			@RequestParam(required = false) @DecimalMin(value = "0.00") BigDecimal minPrice,
			@RequestParam(required = false) @DecimalMin(value = "0.00") BigDecimal maxPrice,
			@RequestParam(defaultValue = "createdAt") String sortBy,
			@RequestParam(defaultValue = "desc") String direction
	) {
		return PageResponse.from(
				retrieveProductsUseCase.list(
						ProductSearchQuery.forCatalog(q, categoryId, minPrice, maxPrice, sortBy, direction),
						new PageRequest(page, size)
				),
				ProductResponse::from
		);
	}

	@GetMapping("/manage")
	@PreAuthorize("hasRole('ADMIN')")
	PageResponse<ProductResponse> listProductsForManagement(
			@RequestParam(defaultValue = "0") @PositiveOrZero(message = "La pagina no puede ser negativa") int page,
			@RequestParam(defaultValue = "20") @Positive @Max(value = 100, message = "El tamano maximo es 100") int size,
			@RequestParam(required = false) @Size(max = 120) String q,
			@RequestParam(required = false) @Positive Long categoryId,
			@RequestParam(required = false) @DecimalMin(value = "0.00") BigDecimal minPrice,
			@RequestParam(required = false) @DecimalMin(value = "0.00") BigDecimal maxPrice,
			@RequestParam(required = false) Boolean active,
			@RequestParam(defaultValue = "createdAt") String sortBy,
			@RequestParam(defaultValue = "desc") String direction
	) {
		return PageResponse.from(
				retrieveProductsUseCase.list(
						ProductSearchQuery.forManagement(q, categoryId, minPrice, maxPrice, active, sortBy, direction),
						new PageRequest(page, size)
				),
				ProductResponse::from
		);
	}

	@GetMapping("/{id}")
	ProductResponse getProduct(@PathVariable Long id) {
		return ProductResponse.from(retrieveProductsUseCase.byId(id));
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.CREATED)
	ProductResponse createProduct(@Valid @RequestBody CreateProductRequest request) {
		return ProductResponse.from(createProductUseCase.execute(request.toCommand()));
	}

	@PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasRole('ADMIN')")
	ProductResponse updateProduct(
			@PathVariable Long id,
			@Valid @RequestBody UpdateProductRequest request
	) {
		return ProductResponse.from(updateProductUseCase.execute(request.toCommand(id)));
	}

	@PostMapping(path = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.CREATED)
	ProductResponse addImages(
			@PathVariable Long id,
			@RequestParam(name = "images") List<MultipartFile> images
	) {
		return ProductResponse.from(createProductImagesUseCase.execute(id, toUploadFiles(images)));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void deleteProduct(@PathVariable Long id) {
		deleteProductUseCase.execute(id);
	}

	private static List<UploadFile> toUploadFiles(List<MultipartFile> files) {
		if (files == null || files.isEmpty()) {
			return List.of();
		}
		return files.stream()
				.map(MultipartUploadFile::new)
				.map(UploadFile.class::cast)
				.toList();
	}
}
