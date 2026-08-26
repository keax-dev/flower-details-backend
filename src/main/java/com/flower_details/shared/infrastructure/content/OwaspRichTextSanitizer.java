package com.flower_details.shared.infrastructure.content;

import com.flower_details.shared.domain.DomainException;
import com.flower_details.shared.domain.content.RichTextSanitizer;
import org.jsoup.Jsoup;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class OwaspRichTextSanitizer implements RichTextSanitizer {

	private static final Pattern TEXT_COLOR = Pattern.compile(
			"color\\s*:\\s*(?:#[0-9a-fA-F]{3}(?:[0-9a-fA-F]{3})?|rgb\\(\\s*(?:[0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\s*,\\s*(?:[0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\s*,\\s*(?:[0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\s*\\))\\s*;?\\s*",
			Pattern.CASE_INSENSITIVE
	);

	private static final PolicyFactory POLICY = new HtmlPolicyBuilder()
			.allowElements("p", "br", "strong", "em", "u", "s", "ul", "ol", "li", "h2", "h3", "blockquote", "span", "a")
			.allowWithoutAttributes("p", "br", "strong", "em", "u", "s", "ul", "ol", "li", "h2", "h3", "blockquote", "span", "a")
			.allowAttributes("style")
			.matching((elementName, attributeName, value) -> TEXT_COLOR.matcher(value).matches() ? value : null)
			.onElements("span")
			.allowAttributes("href")
			.onElements("a")
			.allowUrlProtocols("https", "mailto")
			.requireRelNofollowOnLinks()
			.toFactory();

	@Override
	public String sanitizeDescription(String content, int maxPlainTextLength) {
		String sanitized = POLICY.sanitize(content == null ? "" : content).trim();
		String plainText = Jsoup.parseBodyFragment(sanitized).text().replace('\u00A0', ' ').strip();
		if (plainText.isEmpty()) {
			throw new DomainException("La descripcion es obligatoria");
		}
		if (plainText.length() > maxPlainTextLength) {
			throw new DomainException("La descripcion no puede superar " + maxPlainTextLength + " caracteres");
		}
		return sanitized;
	}
}
