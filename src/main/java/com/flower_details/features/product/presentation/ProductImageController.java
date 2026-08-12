package com.flower_details.features.product.presentation;

import com.flower_details.features.product.application.dto.StoredFileContent;
import com.flower_details.features.product.application.port.in.GetProductImageFileUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/product-images")
@RequiredArgsConstructor
class ProductImageController {

	private final GetProductImageFileUseCase getProductImageFileUseCase;

	@GetMapping("/{storedFileName}")
	ResponseEntity<byte[]> getProductImage(@PathVariable String storedFileName) {
		StoredFileContent file = getProductImageFileUseCase.getProductImageFile(storedFileName);
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(file.contentType()))
				.header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
						.filename(file.fileName())
						.build()
						.toString())
				.cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic())
				.body(file.content());
	}
}
