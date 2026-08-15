package com.flower_details.features.product.presentation.controller;

import com.flower_details.features.product.application.dto.command.CreateProductCommand;
import com.flower_details.features.product.application.dto.command.UpdateProductCommand;
import com.flower_details.features.product.application.dto.storage.UploadFile;
import com.flower_details.features.product.application.service.ProductApplicationService;
import com.flower_details.features.product.presentation.dto.response.ProductResponse;
import com.flower_details.features.product.presentation.upload.MultipartUploadFile;
import com.flower_details.shared.domain.pagination.PageRequest;
import com.flower_details.shared.presentation.PageResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
class ProductController {

	private final ProductApplicationService productApplicationService;

	@GetMapping
	PageResponse<ProductResponse> listProducts(
			@RequestParam(defaultValue = "0") @PositiveOrZero(message = "La pagina no puede ser negativa") int page,
			@RequestParam(defaultValue = "20") @Positive @Max(value = 100, message = "El tamano maximo es 100") int size
	) {
		return PageResponse.from(
				productApplicationService.listActiveProducts(new PageRequest(page, size)),
				ProductResponse::from
		);
	}

	@GetMapping("/{id}")
	ProductResponse getProduct(@PathVariable Long id) {
		return ProductResponse.from(productApplicationService.getActiveProduct(id));
	}

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.CREATED)
	ProductResponse createProduct(
			@RequestParam @NotNull(message = "La categoria es obligatoria") Long categoryId,
			@RequestParam @NotBlank(message = "El titulo es obligatorio")
			@Size(max = 160, message = "El titulo no puede superar 160 caracteres") String title,
			@RequestParam @NotBlank(message = "La descripcion es obligatoria")
			@Size(max = 1000, message = "La descripcion no puede superar 1000 caracteres") String description,
			@RequestParam @NotNull(message = "El precio es obligatorio")
			@Positive(message = "El precio debe ser mayor a cero") BigDecimal price,
			@RequestParam(defaultValue = "true") boolean active,
			@RequestParam(name = "images", required = false) List<MultipartFile> images
	) {
		return ProductResponse.from(productApplicationService.createProduct(new CreateProductCommand(
				categoryId,
				title,
				description,
				price,
				active,
				toUploadFiles(images)
		)));
	}

	@PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("hasRole('ADMIN')")
	ProductResponse updateProduct(
			@PathVariable Long id,
			@RequestParam @NotNull(message = "La categoria es obligatoria") Long categoryId,
			@RequestParam @NotBlank(message = "El titulo es obligatorio")
			@Size(max = 160, message = "El titulo no puede superar 160 caracteres") String title,
			@RequestParam @NotBlank(message = "La descripcion es obligatoria")
			@Size(max = 1000, message = "La descripcion no puede superar 1000 caracteres") String description,
			@RequestParam @NotNull(message = "El precio es obligatorio")
			@Positive(message = "El precio debe ser mayor a cero") BigDecimal price,
			@RequestParam @NotNull(message = "El estado activo es obligatorio") Boolean active,
			@RequestParam(name = "images", required = false) List<MultipartFile> images
	) {
		return ProductResponse.from(productApplicationService.updateProduct(new UpdateProductCommand(
				id,
				categoryId,
				title,
				description,
				price,
				active,
				toUploadFiles(images)
		)));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void deleteProduct(@PathVariable Long id) {
		productApplicationService.deleteProduct(id);
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
