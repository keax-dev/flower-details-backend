package com.flower_details.features.auth.infrastructure.security.configuration;

import jakarta.servlet.http.HttpServletResponse;
import com.flower_details.features.auth.infrastructure.security.cookie.AuthCookieManager;
import com.flower_details.features.auth.infrastructure.security.jwt.JwtAuthenticationFilter;
import com.flower_details.features.auth.infrastructure.security.ratelimit.AuthenticationRateLimitFilter;
import com.flower_details.shared.infrastructure.observability.RequestIdFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;

@Configuration
@EnableMethodSecurity
class SecurityConfig {

	private final AuthCookieManager authCookieManager;
	private final CorsProperties corsProperties;

	SecurityConfig(AuthCookieManager authCookieManager, CorsProperties corsProperties) {
		this.authCookieManager = authCookieManager;
		this.corsProperties = corsProperties;
	}

	@Bean
	SecurityFilterChain securityFilterChain(
			HttpSecurity http,
			JwtAuthenticationFilter jwtAuthenticationFilter,
			AuthenticationRateLimitFilter authenticationRateLimitFilter,
			RequestIdFilter requestIdFilter
	)
			throws Exception {
		return http
				.cors(cors -> {})
				.csrf(csrf -> csrf
						.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
						.csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
						.requireCsrfProtectionMatcher(request -> requiresCsrfProtection(request))
				)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(exceptions -> exceptions
						.authenticationEntryPoint((request, response, authException) -> {
							response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
							response.setContentType(MediaType.APPLICATION_JSON_VALUE);
							response.getWriter().write("{\"message\":\"No autenticado\"}");
						})
						.accessDeniedHandler((request, response, accessDeniedException) -> {
							boolean csrfFailure = accessDeniedException instanceof CsrfException;
							response.setStatus(HttpServletResponse.SC_FORBIDDEN);
							response.setContentType(MediaType.APPLICATION_JSON_VALUE);
							response.getWriter().write(csrfFailure
									? "{\"message\":\"Solicitud rechazada por seguridad\",\"code\":\"CSRF_TOKEN_INVALID\"}"
									: "{\"message\":\"No tienes permisos para esta accion\",\"code\":\"ACCESS_DENIED\"}");
						})
				)
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/health/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
						.requestMatchers(
								HttpMethod.GET,
								"/api/categories",
								"/api/products",
								"/api/products/*",
								"/api/product-images/*"
						).permitAll()
						.requestMatchers(
								HttpMethod.POST,
								"/api/auth/register",
								"/api/auth/login",
								"/api/auth/logout"
						).permitAll()
						.requestMatchers(HttpMethod.GET, "/api/auth/csrf").permitAll()
						.anyRequest().authenticated()
				)
				.addFilterBefore(requestIdFilter, SecurityContextHolderFilter.class)
				.addFilterBefore(authenticationRateLimitFilter, UsernamePasswordAuthenticationFilter.class)
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
				.build();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(corsProperties.origins());
		configuration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(java.util.List.of("Content-Type", "X-XSRF-TOKEN", "Authorization", "X-Request-Id"));
		configuration.setExposedHeaders(java.util.List.of("X-Request-Id"));
		configuration.setAllowCredentials(true);
		configuration.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	private boolean requiresCsrfProtection(jakarta.servlet.http.HttpServletRequest request) {
		if (HttpMethod.GET.matches(request.getMethod())
				|| HttpMethod.HEAD.matches(request.getMethod())
				|| HttpMethod.OPTIONS.matches(request.getMethod())
				|| HttpMethod.TRACE.matches(request.getMethod())) {
			return false;
		}

		String authorization = request.getHeader(org.springframework.http.HttpHeaders.AUTHORIZATION);
		boolean usesBearerToken = authorization != null && authorization.startsWith("Bearer ");
		return !usesBearerToken && authCookieManager.hasAccessToken(request);
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(12);
	}

	@Bean
	FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilterRegistration(
			JwtAuthenticationFilter jwtAuthenticationFilter
	) {
		FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(jwtAuthenticationFilter);
		registration.setEnabled(false);
		return registration;
	}

	@Bean
	FilterRegistrationBean<AuthenticationRateLimitFilter> authenticationRateLimitFilterRegistration(
			AuthenticationRateLimitFilter authenticationRateLimitFilter
	) {
		FilterRegistrationBean<AuthenticationRateLimitFilter> registration = new FilterRegistrationBean<>(authenticationRateLimitFilter);
		registration.setEnabled(false);
		return registration;
	}

	@Bean
	FilterRegistrationBean<RequestIdFilter> requestIdFilterRegistration(RequestIdFilter requestIdFilter) {
		FilterRegistrationBean<RequestIdFilter> registration = new FilterRegistrationBean<>(requestIdFilter);
		registration.setEnabled(false);
		return registration;
	}
}
