package com.flower_details.shared.domain.content;

public interface RichTextSanitizer {

	String sanitizeDescription(String content, int maxPlainTextLength);
}
