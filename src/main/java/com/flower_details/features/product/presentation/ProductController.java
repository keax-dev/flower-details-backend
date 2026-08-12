package com.flower_details.features.product.presentation;

import com.flower_details.features.product.application.dto.CreateProductCommand;
import com.flower_details.features.product.application.dto.UpdateProductCommand;
import com.flower_details.features.product.application.dto.UploadFile;
import com.flower_details.features.product.application.port.in.CreateProductUseCase;
import com.flower_details.features.product.application.port.in.DeleteProductUseCase;
import com.flower_details.features.product.application.port.in.GetProductUseCase;
import com.flower_details.features.product.application.port.in.ListProductsUseCase;
import com.flower_details.features.product.application.port.in.UpdateProductUseCase;
import com.flower_details.features.product.presentation.dto.ProductResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/products")
class ProductController {

	private final ListProductsUseCase listProductsUseCase;
	private final GetProductUseCase getProductUseCase;
	private final CreateProductUseCase createProductUseCase;
	private final UpdateProductUseCase updateProductUseCase;
	private final DeleteProductUseCase deleteProductUseCase;

	ProductController(
			ListProductsUseCase listProductsUseCase,
			GetProductUseCase getProductUseCase,
			CreateProductUseCase createProductUseCase,
			UpdateProductUseCase updateProductUseCase,
			DeleteProductUseCase deleteProductUseCase
	) {
		this.listProductsUseCase = listProductsUseCase;
		this.getProductUseCase = getProductUseCase;
		this.createProductUseCase = createProductUseCase;
		this.updateProductUseCase = updateProductUseCase;
		this.deleteProductUseCase = deleteProductUseCase;
	}

	@GetMapping
	List<ProductResponse> listProducts() {
		return listProductsUseCase.listActiveProducts()
				.stream()
				.map(ProductResponse::from)
				.toList();
	}

	@GetMapping("/{id}")
	ProductResponse getProduct(@PathVariable Long id) {
		return ProductResponse.from(getProductUseCase.getActiveProduct(id));
	}

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("hasRole('ADMIN')")
	ResponseEntity<ProductResponse> createProduct(
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
		ProductResponse response = ProductResponse.from(createProductUseCase.createProduct(new CreateProductCommand(
				categoryId,
				title,
				description,
				price,
				active,
				toUploadFiles(images)
		)));
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
		return ProductResponse.from(updateProductUseCase.updateProduct(new UpdateProductCommand(
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
	ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
		deleteProductUseCase.deleteProduct(id);
		return ResponseEntity.noContent().build();
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
