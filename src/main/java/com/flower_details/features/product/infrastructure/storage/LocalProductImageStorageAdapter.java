package com.flower_details.features.product.infrastructure.storage;

import com.flower_details.features.product.application.dto.StoredFile;
import com.flower_details.features.product.application.dto.StoredFileContent;
import com.flower_details.features.product.application.dto.UploadFile;
import com.flower_details.features.product.application.exception.FileStorageException;
import com.flower_details.features.product.application.port.out.ProductImageStoragePort;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Component
class LocalProductImageStorageAdapter implements ProductImageStoragePort {

	private final ProductImageStorageProperties properties;

	LocalProductImageStorageAdapter(ProductImageStorageProperties properties) {
		this.properties = properties;
	}

	@Override
	public StoredFile store(UploadFile file) {
		validate(file);

		String contentType = file.contentType().trim().toLowerCase(Locale.ROOT);
		String storedFileName = UUID.randomUUID() + extensionFor(contentType);
		Path target = properties.normalizedRootPath().resolve(storedFileName).normalize();
		if (!target.startsWith(properties.normalizedRootPath())) {
			throw new FileStorageException("Ruta de imagen invalida");
		}

		try {
			Files.createDirectories(properties.normalizedRootPath());
			try (InputStream inputStream = file.inputStream()) {
				Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
			}
		}
		catch (IOException exception) {
			throw new FileStorageException("No se pudo almacenar la imagen del producto", exception);
		}

		return new StoredFile(
				properties.normalizedPublicUrlPath() + "/" + storedFileName,
				storedFileName,
				normalizeOriginalFilename(file.originalFilename()),
				contentType,
				file.size()
		);
	}

	@Override
	public StoredFileContent load(String storedFileName) {
		Path target = properties.normalizedRootPath().resolve(storedFileName).normalize();
		if (!target.startsWith(properties.normalizedRootPath()) || !Files.exists(target)) {
			throw new FileStorageException("No se encontro la imagen solicitada");
		}

		try {
			String contentType = Files.probeContentType(target);
			if (contentType == null || contentType.isBlank()) {
				contentType = "application/octet-stream";
			}
			return new StoredFileContent(storedFileName, contentType, Files.readAllBytes(target));
		}
		catch (IOException exception) {
			throw new FileStorageException("No se pudo leer la imagen solicitada", exception);
		}
	}

	@Override
	public void delete(String storedFileName) {
		Path target = properties.normalizedRootPath().resolve(storedFileName).normalize();
		if (!target.startsWith(properties.normalizedRootPath())) {
			throw new FileStorageException("Ruta de imagen invalida");
		}
		try {
			Files.deleteIfExists(target);
		}
		catch (IOException exception) {
			throw new FileStorageException("No se pudo eliminar la imagen almacenada", exception);
		}
	}

	private void validate(UploadFile file) {
		if (file == null || file.isEmpty()) {
			throw new FileStorageException("La imagen del producto es obligatoria");
		}
		if (file.size() > properties.maxSizeBytes()) {
			throw new FileStorageException("La imagen supera el tamano maximo permitido");
		}
		String contentType = file.contentType();
		if (contentType == null || contentType.isBlank()) {
			throw new FileStorageException("La imagen debe tener un tipo de contenido valido");
		}
		Set<String> allowedTypes = properties.allowedContentTypeSet();
		if (!allowedTypes.contains(contentType.trim().toLowerCase(Locale.ROOT))) {
			throw new FileStorageException("El tipo de imagen no esta permitido");
		}
	}

	private static String extensionFor(String contentType) {
		return switch (contentType) {
			case "image/jpeg" -> ".jpg";
			case "image/png" -> ".png";
			case "image/webp" -> ".webp";
			default -> throw new FileStorageException("El tipo de imagen no esta permitido");
		};
	}

	private static String normalizeOriginalFilename(String originalFilename) {
		if (originalFilename == null || originalFilename.isBlank()) {
			return "product-image";
		}
		return Path.of(originalFilename).getFileName().toString();
	}
}
