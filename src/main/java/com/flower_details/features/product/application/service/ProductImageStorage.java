package com.flower_details.features.product.application.service;

import com.flower_details.features.product.application.dto.storage.StoredFile;
import com.flower_details.features.product.application.dto.storage.StoredFileContent;
import com.flower_details.features.product.application.dto.storage.UploadFile;

public interface ProductImageStorage {

	StoredFile store(UploadFile file);

	StoredFileContent load(String storedFileName);

	void delete(String storedFileName);
}
