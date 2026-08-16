package com.flower_details.features.auth.infrastructure.security.jwt;

import com.flower_details.features.auth.infrastructure.security.cookie.AuthCookieManager;
import com.flower_details.features.auth.infrastructure.security.identity.AuthenticatedUserPrincipal;
import com.flower_details.features.users.domain.model.User;
import com.flower_details.features.users.domain.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String BEARER_PREFIX = "Bearer ";

	private final JwtTokenService jwtTokenService;
	private final UserRepository userRepository;
	private final AuthCookieManager authCookieManager;

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {
		if (SecurityContextHolder.getContext().getAuthentication() == null) {
			resolveToken(request).ifPresent(token -> authenticate(token, request));
		}

		filterChain.doFilter(request, response);
	}

	private Optional<String> resolveToken(HttpServletRequest request) {
		String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (authorization != null && authorization.startsWith(BEARER_PREFIX)) {
			return Optional.of(authorization.substring(BEARER_PREFIX.length()));
		}
		return authCookieManager.resolveAccessToken(request);
	}

	private void authenticate(String token, HttpServletRequest request) {
		jwtTokenService.validate(token)
				.flatMap(claims -> userRepository.findById(claims.userId()))
				.filter(User::active)
				.ifPresent(user -> {
			AuthenticatedUserPrincipal principal = new AuthenticatedUserPrincipal(
					user.id(),
					user.email(),
					user.role()
			);
			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
					principal,
					null,
					principal.getAuthorities()
			);
			authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			SecurityContextHolder.getContext().setAuthentication(authentication);
		});
	}
}
