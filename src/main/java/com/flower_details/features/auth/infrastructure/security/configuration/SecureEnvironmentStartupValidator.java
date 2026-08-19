package com.flower_details.features.auth.infrastructure.security.configuration;

import com.flower_details.features.auth.infrastructure.security.cookie.AuthCookieProperties;
import com.flower_details.features.auth.infrastructure.security.ratelimit.AuthenticationRateLimitProperties;
import com.flower_details.features.users.infrastructure.bootstrap.AdminBootstrapProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;

@Component
@Profile("!dev & !test")
@RequiredArgsConstructor
class SecureEnvironmentStartupValidator implements ApplicationRunner {

	private final AuthCookieProperties cookieProperties;
	private final CorsProperties corsProperties;
	private final AuthenticationRateLimitProperties rateLimitProperties;
	private final AdminBootstrapProperties adminBootstrapProperties;
	private final RedisConnectionFactory redisConnectionFactory;

	@Override
	public void run(ApplicationArguments args) {
		validateSecurityConfiguration();
		verifyRedisConnection();
	}

	private void validateSecurityConfiguration() {
		if (!cookieProperties.secure()) {
			throw new IllegalStateException("La cookie de autenticacion debe usar Secure fuera de desarrollo");
		}
		if (!List.of("Lax", "Strict", "None").contains(cookieProperties.sameSite())) {
			throw new IllegalStateException("security.cookie.same-site debe ser Lax, Strict o None");
		}
		if (adminBootstrapProperties.enabled()) {
			throw new IllegalStateException("El bootstrap de administrador debe estar deshabilitado fuera de desarrollo");
		}
		if (!"redis".equals(rateLimitProperties.backend())) {
			throw new IllegalStateException("El rate limiting Redis es obligatorio fuera de desarrollo");
		}
		if (corsProperties.origins().isEmpty() || corsProperties.origins().stream().anyMatch(origin -> !isSecureOrigin(origin))) {
			throw new IllegalStateException("security.cors.allowed-origins debe contener origenes HTTPS explicitos");
		}
	}

	private void verifyRedisConnection() {
		try (RedisConnection connection = redisConnectionFactory.getConnection()) {
			if (!"PONG".equalsIgnoreCase(connection.ping())) {
				throw new IllegalStateException("Redis no respondio correctamente durante el arranque");
			}
		}
		catch (Exception exception) {
			throw new IllegalStateException("No se pudo conectar a Redis durante el arranque", exception);
		}
	}

	private static boolean isSecureOrigin(String origin) {
		try {
			URI uri = URI.create(origin);
			return "https".equalsIgnoreCase(uri.getScheme())
					&& uri.getHost() != null
					&& (uri.getPath() == null || uri.getPath().isEmpty() || "/".equals(uri.getPath()))
					&& uri.getQuery() == null
					&& uri.getFragment() == null;
		}
		catch (IllegalArgumentException exception) {
			return false;
		}
	}
}
