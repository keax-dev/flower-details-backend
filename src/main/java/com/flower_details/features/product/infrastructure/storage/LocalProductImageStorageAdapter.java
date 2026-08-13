package com.flower_details.features.product.infrastructure.storage;

import com.flower_details.features.product.application.dto.StoredFile;
import com.flower_details.features.product.application.dto.StoredFileContent;
import com.flower_details.features.product.application.dto.UploadFile;
import com.flower_details.features.product.application.exception.FileStorageException;
import com.flower_details.features.product.application.port.out.ProductImageStoragePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
class LocalProductImageStorageAdapter implements ProductImageStoragePort {

	private final ProductImageStorageProperties properties;

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
		validateFileSignature(file, contentType.trim().toLowerCase(Locale.ROOT));
	}

	private void validateFileSignature(UploadFile file, String contentType) {
		try (InputStream inputStream = new BufferedInputStream(file.inputStream())) {
			byte[] header = inputStream.readNBytes(12);
			if (!hasValidSignature(header, contentType)) {
				throw new FileStorageException("El contenido del archivo no corresponde a una imagen valida");
			}
		}
		catch (IOException exception) {
			throw new FileStorageException("No se pudo validar la imagen del producto", exception);
		}
	}

	private static boolean hasValidSignature(byte[] header, String contentType) {
		return switch (contentType) {
			case "image/png" -> header.length >= 8
					&& header[0] == (byte) 0x89
					&& header[1] == 0x50
					&& header[2] == 0x4E
					&& header[3] == 0x47
					&& header[4] == 0x0D
					&& header[5] == 0x0A
					&& header[6] == 0x1A
					&& header[7] == 0x0A;
			case "image/jpeg" -> header.length >= 3
					&& header[0] == (byte) 0xFF
					&& header[1] == (byte) 0xD8
					&& header[2] == (byte) 0xFF;
			case "image/webp" -> header.length >= 12
					&& header[0] == 0x52
					&& header[1] == 0x49
					&& header[2] == 0x46
					&& header[3] == 0x46
					&& header[8] == 0x57
					&& header[9] == 0x45
					&& header[10] == 0x42
					&& header[11] == 0x50;
			default -> false;
		};
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
