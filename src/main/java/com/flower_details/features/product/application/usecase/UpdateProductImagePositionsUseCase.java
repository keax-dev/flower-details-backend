package com.flower_details.features.product.application.usecase;

import com.flower_details.features.category.application.exception.CategoryNotFoundException;
import com.flower_details.features.category.domain.model.Category;
import com.flower_details.features.category.domain.repository.CategoryRepository;
import com.flower_details.features.product.application.dto.command.ProductImagePositionCommand;
import com.flower_details.features.product.application.dto.command.UpdateProductImagePositionsCommand;
import com.flower_details.features.product.application.dto.view.ProductView;
import com.flower_details.features.product.application.exception.ProductNotFoundException;
import com.flower_details.features.product.domain.model.Product;
import com.flower_details.features.product.domain.model.ProductImage;
import com.flower_details.features.product.domain.repository.ProductImageRepository;
import com.flower_details.features.product.domain.repository.ProductRepository;
import com.flower_details.shared.domain.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class UpdateProductImagePositionsUseCase {

	private final ProductRepository productRepository;
	private final ProductImageRepository productImageRepository;
	private final CategoryRepository categoryRepository;

	@Transactional
	public ProductView execute(UpdateProductImagePositionsCommand command) {
		Product product = productRepository.findById(command.productId())
				.orElseThrow(() -> new ProductNotFoundException(command.productId()));
		List<ProductImage> images = productImageRepository.findActiveByProductId(product.id());
		Map<Long, Integer> positionsByImageId = positionsByImageId(command.positions());
		validatePositions(images, positionsByImageId);

		List<ProductImage> reorderedImages = images.stream()
				.map(image -> image.withSortOrder(positionsByImageId.get(image.id())))
				.sorted(java.util.Comparator.comparingInt(ProductImage::sortOrder))
				.toList();
		List<ProductImage> savedImages = productImageRepository.saveAll(reorderedImages);
		Category category = categoryRepository.findById(product.categoryId())
				.orElseThrow(() -> new CategoryNotFoundException(product.categoryId()));
		return ProductView.from(product, category, savedImages);
	}

	private static Map<Long, Integer> positionsByImageId(List<ProductImagePositionCommand> positions) {
		return positions.stream().collect(Collectors.toMap(
				ProductImagePositionCommand::imageId,
				ProductImagePositionCommand::sortOrder,
				(first, duplicate) -> {
					throw new DomainException("Una imagen no puede tener mas de una posicion");
				}
		));
	}

	private static void validatePositions(List<ProductImage> images, Map<Long, Integer> positionsByImageId) {
		Set<Long> imageIds = images.stream().map(ProductImage::id).collect(Collectors.toSet());
		if (!imageIds.equals(positionsByImageId.keySet())) {
			throw new DomainException("Debes enviar las posiciones de todas las imagenes activas del producto");
		}

		Set<Integer> expectedPositions = IntStream.range(0, images.size()).boxed().collect(Collectors.toSet());
		if (!expectedPositions.equals(Set.copyOf(positionsByImageId.values()))) {
			throw new DomainException("Las posiciones deben ser consecutivas, unicas y comenzar en cero");
		}
	}
}
