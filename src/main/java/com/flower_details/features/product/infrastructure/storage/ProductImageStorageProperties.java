package com.flower_details.features.product.infrastructure.storage;

import com.flower_details.features.product.application.exception.FileStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Component
record ProductImageStorageProperties(
		@Value("${app.storage.products.root-path:uploads/products}")
		String rootPath,

		@Value("${app.storage.products.public-url-path:/api/product-images}")
		String publicUrlPath,

		@Value("${app.storage.products.max-size-bytes:5242880}")
		long maxSizeBytes,

		@Value("${app.storage.products.allowed-content-types:image/jpeg,image/png,image/webp}")
		String allowedContentTypes
) {

	ProductImageStorageProperties {
		if (maxSizeBytes <= 0) {
			throw new FileStorageException("app.storage.products.max-size-bytes debe ser mayor a cero");
		}
	}

	Path normalizedRootPath() {
		return Path.of(rootPath).toAbsolutePath().normalize();
	}

	String normalizedPublicUrlPath() {
		String normalized = publicUrlPath == null || publicUrlPath.isBlank()
				? "/api/product-images"
				: publicUrlPath.trim();
		if (normalized.endsWith("/")) {
			return normalized.substring(0, normalized.length() - 1);
		}
		return normalized;
	}

	Set<String> allowedContentTypeSet() {
		return Arrays.stream(allowedContentTypes.split(","))
				.map(String::trim)
				.filter(value -> !value.isBlank())
				.collect(Collectors.toUnmodifiableSet());
	}
}
