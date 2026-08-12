package com.flower_details.features.product.application.port.in;

import com.flower_details.features.product.application.dto.StoredFileContent;

public interface GetProductImageFileUseCase {

	StoredFileContent getProductImageFile(String storedFileName);
}
