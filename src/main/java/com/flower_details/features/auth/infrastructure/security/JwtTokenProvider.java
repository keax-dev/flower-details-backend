package com.flower_details.features.auth.infrastructure.security;

import com.flower_details.features.auth.application.port.out.TokenClaims;
import com.flower_details.features.auth.application.port.out.TokenProviderPort;
import com.flower_details.features.users.domain.model.User;
import com.flower_details.features.users.domain.model.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
class JwtTokenProvider implements TokenProviderPort {

	private static final String HMAC_ALGORITHM = "HmacSHA256";
	private static final TypeReference<Map<String, Object>> CLAIMS_TYPE = new TypeReference<>() {
	};

	private final JwtProperties properties;
	private final ObjectMapper objectMapper;

	@Override
	public String generate(User user) {
		Instant now = Instant.now();
		Instant expiresAt = now.plus(properties.expiration());

		Map<String, Object> header = new LinkedHashMap<>();
		header.put("alg", "HS256");
		header.put("typ", "JWT");

		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("sub", user.email());
		payload.put("userId", user.id());
		payload.put("role", user.role().name());
		payload.put("iat", now.getEpochSecond());
		payload.put("exp", expiresAt.getEpochSecond());

		String signingInput = encodeJson(header) + "." + encodeJson(payload);
		return signingInput + "." + signAsBase64Url(signingInput);
	}

	@Override
	public Optional<TokenClaims> validate(String token) {
		try {
			String[] parts = token.split("\\.");
			if (parts.length != 3 || !hasValidSignature(parts)) {
				return Optional.empty();
			}

			Map<String, Object> claims = objectMapper.readValue(
					Base64.getUrlDecoder().decode(parts[1]),
					CLAIMS_TYPE
			);

			long expiration = getLong(claims, "exp");
			if (expiration <= Instant.now().getEpochSecond()) {
				return Optional.empty();
			}

			return Optional.of(new TokenClaims(
					getLong(claims, "userId"),
					getString(claims, "sub"),
					UserRole.valueOf(getString(claims, "role"))
			));
		}
		catch (RuntimeException ex) {
			return Optional.empty();
		}
	}

	@Override
	public long expirationSeconds() {
		return properties.expiration().toSeconds();
	}

	private boolean hasValidSignature(String[] parts) {
		String signingInput = parts[0] + "." + parts[1];
		String expectedSignature = signAsBase64Url(signingInput);
		return MessageDigest.isEqual(
				expectedSignature.getBytes(StandardCharsets.US_ASCII),
				parts[2].getBytes(StandardCharsets.US_ASCII)
		);
	}

	private String encodeJson(Map<String, Object> value) {
		try {
			return Base64.getUrlEncoder()
					.withoutPadding()
					.encodeToString(objectMapper.writeValueAsBytes(value));
		}
		catch (JacksonException ex) {
			throw new IllegalStateException("No se pudo serializar el token JWT", ex);
		}
	}

	private String signAsBase64Url(String signingInput) {
		return Base64.getUrlEncoder()
				.withoutPadding()
				.encodeToString(sign(signingInput));
	}

	private byte[] sign(String signingInput) {
		try {
			Mac mac = Mac.getInstance(HMAC_ALGORITHM);
			mac.init(new SecretKeySpec(properties.secret().getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
			return mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8));
		}
		catch (Exception ex) {
			throw new IllegalStateException("No se pudo firmar el token JWT", ex);
		}
	}

	private static String getString(Map<String, Object> claims, String key) {
		Object value = claims.get(key);
		if (value instanceof String text && !text.isBlank()) {
			return text;
		}
		throw new IllegalArgumentException("Claim JWT invalido: " + key);
	}

	private static long getLong(Map<String, Object> claims, String key) {
		Object value = claims.get(key);
		if (value instanceof Number number) {
			return number.longValue();
		}
		throw new IllegalArgumentException("Claim JWT invalido: " + key);
	}
}
