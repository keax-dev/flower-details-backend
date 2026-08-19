package com.flower_details.features.auth.infrastructure.security.ratelimit;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthenticationRateLimitFilterTest {

	@Test
	void onlySuccessfulLoginsResetTheirOwnRateLimitBucket() throws Exception {
		AuthenticationAttemptRateLimiter rateLimiter = mock(AuthenticationAttemptRateLimiter.class);
		when(rateLimiter.tryConsume(anyString())).thenReturn(RateLimitDecision.permit());
		AuthenticationRateLimitFilter filter = new AuthenticationRateLimitFilter(
				rateLimiter,
				new ClientIpResolver(new TrustedProxyProperties(""))
		);

		MockHttpServletRequest registerRequest = request("/api/auth/register", "203.0.113.9");
		filter.doFilter(
				registerRequest,
				new MockHttpServletResponse(),
				(request, response) -> ((MockHttpServletResponse) response).setStatus(201)
		);

		verify(rateLimiter).tryConsume("/api/auth/register:203.0.113.9");
		verify(rateLimiter, never()).reset(anyString());

		MockHttpServletRequest loginRequest = request("/api/auth/login", "203.0.113.9");
		filter.doFilter(
				loginRequest,
				new MockHttpServletResponse(),
				(request, response) -> ((MockHttpServletResponse) response).setStatus(200)
		);

		verify(rateLimiter).tryConsume("/api/auth/login:203.0.113.9");
		verify(rateLimiter).reset("/api/auth/login:203.0.113.9");
	}

	@Test
	void onlyTrustedProxiesCanProvideTheClientAddress() {
		ClientIpResolver resolver = new ClientIpResolver(new TrustedProxyProperties("127.0.0.1,10.0.0.0/8"));

		MockHttpServletRequest proxiedRequest = request("/api/auth/login", "127.0.0.1");
		proxiedRequest.addHeader("X-Forwarded-For", "198.51.100.30, 10.10.2.4");
		MockHttpServletRequest directRequest = request("/api/auth/login", "203.0.113.20");
		directRequest.addHeader("X-Forwarded-For", "198.51.100.30");

		org.assertj.core.api.Assertions.assertThat(resolver.resolve(proxiedRequest)).isEqualTo("198.51.100.30");
		org.assertj.core.api.Assertions.assertThat(resolver.resolve(directRequest)).isEqualTo("203.0.113.20");
	}

	private static MockHttpServletRequest request(String path, String remoteAddress) {
		MockHttpServletRequest request = new MockHttpServletRequest(HttpMethod.POST.name(), path);
		request.setRemoteAddr(remoteAddress);
		return request;
	}
}
