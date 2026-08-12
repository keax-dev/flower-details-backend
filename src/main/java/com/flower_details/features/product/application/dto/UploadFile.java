package com.flower_details.features.product.application.dto;

import java.io.IOException;
import java.io.InputStream;

public interface UploadFile {

	String originalFilename();

	String contentType();

	long size();

	boolean isEmpty();

	InputStream inputStream() throws IOException;
}
