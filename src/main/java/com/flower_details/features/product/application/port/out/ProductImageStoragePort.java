package com.flower_details.features.product.application.port.out;

import com.flower_details.features.product.application.dto.StoredFile;
import com.flower_details.features.product.application.dto.StoredFileContent;
import com.flower_details.features.product.application.dto.UploadFile;

public interface ProductImageStoragePort {

	StoredFile store(UploadFile file);

	StoredFileContent load(String storedFileName);

	void delete(String storedFileName);
}
