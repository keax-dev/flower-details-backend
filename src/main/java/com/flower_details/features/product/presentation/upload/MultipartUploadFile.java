package com.flower_details.features.product.presentation.upload;

import com.flower_details.features.product.application.dto.storage.UploadFile;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@RequiredArgsConstructor
public class MultipartUploadFile implements UploadFile {

	private final MultipartFile file;

	@Override
	public String originalFilename() {
		return file.getOriginalFilename();
	}

	@Override
	public String contentType() {
		return file.getContentType();
	}

	@Override
	public long size() {
		return file.getSize();
	}

	@Override
	public boolean isEmpty() {
		return file.isEmpty();
	}

	@Override
	public InputStream inputStream() throws IOException {
		return file.getInputStream();
	}
}
