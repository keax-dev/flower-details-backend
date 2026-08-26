package com.flower_details.shared.infrastructure.content;

import com.flower_details.shared.domain.DomainException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OwaspRichTextSanitizerTest {

	private final OwaspRichTextSanitizer sanitizer = new OwaspRichTextSanitizer();

	@Test
	void keepsAllowedFormattingAndTextColor() {
		String sanitized = sanitizer.sanitizeDescription(
				"<p><strong>Rosas</strong> <span style=\"color: #198754\">frescas</span></p>",
				500
		);

		assertThat(sanitized).contains("<strong>Rosas</strong>");
		assertThat(sanitized).contains("#198754");
	}

	@Test
	void removesExecutableMarkupAndUnsafeLinks() {
		String sanitized = sanitizer.sanitizeDescription(
				"<p onclick=\"alert(1)\">Detalle</p><script>alert(1)</script><a href=\"javascript:alert(1)\">Abrir</a>",
				500
		);

		assertThat(sanitized)
				.contains("Detalle")
				.doesNotContain("onclick", "<script", "javascript:");
	}

	@Test
	void rejectsDescriptionsWithoutVisibleText() {
		assertThatThrownBy(() -> sanitizer.sanitizeDescription("<p><br></p>", 500))
				.isInstanceOf(DomainException.class)
				.hasMessage("La descripcion es obligatoria");
		assertThatThrownBy(() -> sanitizer.sanitizeDescription("<p>&nbsp;</p>", 500))
				.isInstanceOf(DomainException.class)
				.hasMessage("La descripcion es obligatoria");
	}

	@Test
	void validatesTheVisibleTextLengthInsteadOfHtmlLength() {
		String description = "<p><strong>" + "a".repeat(500) + "</strong></p>";

		assertThat(sanitizer.sanitizeDescription(description, 500)).contains("<strong>");
		assertThatThrownBy(() -> sanitizer.sanitizeDescription(description + "<p>b</p>", 500))
				.isInstanceOf(DomainException.class)
				.hasMessage("La descripcion no puede superar 500 caracteres");
	}
}
