package com.flower_details.shared.infrastructure.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
public class RequestIdFilter extends OncePerRequestFilter {

	public static final String REQUEST_ID_ATTRIBUTE = "requestId";
	private static final String REQUEST_ID_HEADER = "X-Request-Id";
	private static final Pattern VALID_REQUEST_ID = Pattern.compile("[A-Za-z0-9._-]{1,100}");

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {
		String requestId = resolveRequestId(request);
		request.setAttribute(REQUEST_ID_ATTRIBUTE, requestId);
		response.setHeader(REQUEST_ID_HEADER, requestId);

		try (MDC.MDCCloseable ignored = MDC.putCloseable(REQUEST_ID_ATTRIBUTE, requestId)) {
			filterChain.doFilter(request, response);
		}
	}

	private static String resolveRequestId(HttpServletRequest request) {
		String incomingRequestId = request.getHeader(REQUEST_ID_HEADER);
		if (incomingRequestId != null && VALID_REQUEST_ID.matcher(incomingRequestId).matches()) {
			return incomingRequestId;
		}

		return UUID.randomUUID().toString();
	}
}
