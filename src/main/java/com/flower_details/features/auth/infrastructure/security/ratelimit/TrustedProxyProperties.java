package com.flower_details.features.auth.infrastructure.security.ratelimit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
class TrustedProxyProperties {

	private final List<CidrRange> trustedRanges;

	TrustedProxyProperties(@Value("${security.proxy.trusted-addresses:}") String trustedAddresses) {
		trustedRanges = Arrays.stream(trustedAddresses.split(","))
				.map(String::trim)
				.filter(value -> !value.isBlank())
				.map(CidrRange::parse)
				.toList();
	}

	boolean isTrusted(String address) {
		return parseAddress(address)
				.map(candidate -> trustedRanges.stream().anyMatch(range -> range.matches(candidate)))
				.orElse(false);
	}

	boolean isIpAddress(String address) {
		return parseAddress(address).isPresent();
	}

	private static Optional<byte[]> parseAddress(String value) {
		if (value == null || value.isBlank()) {
			return Optional.empty();
		}

		String candidate = value.trim();
		if (!candidate.matches("[0-9a-fA-F:.]+") || (!candidate.contains(".") && !candidate.contains(":"))) {
			return Optional.empty();
		}

		try {
			return Optional.of(InetAddress.getByName(candidate).getAddress());
		}
		catch (UnknownHostException exception) {
			return Optional.empty();
		}
	}

	private record CidrRange(byte[] network, int prefixLength) {

		static CidrRange parse(String value) {
			String[] parts = value.split("/", -1);
			if (parts.length > 2) {
				throw new IllegalArgumentException("Una direccion de proxy confiable no es valida: " + value);
			}

			byte[] network = parseAddress(parts[0])
					.orElseThrow(() -> new IllegalArgumentException("Una direccion de proxy confiable no es valida: " + value));
			int maximumPrefixLength = network.length * Byte.SIZE;
			int prefixLength = parts.length == 1 ? maximumPrefixLength : parsePrefixLength(parts[1], maximumPrefixLength, value);
			return new CidrRange(network, prefixLength);
		}

		boolean matches(byte[] candidate) {
			if (candidate.length != network.length) {
				return false;
			}

			int completeBytes = prefixLength / Byte.SIZE;
			for (int index = 0; index < completeBytes; index++) {
				if (candidate[index] != network[index]) {
					return false;
				}
			}

			int remainingBits = prefixLength % Byte.SIZE;
			if (remainingBits == 0) {
				return true;
			}

			int mask = 0xFF << (Byte.SIZE - remainingBits);
			return (candidate[completeBytes] & mask) == (network[completeBytes] & mask);
		}

		private static int parsePrefixLength(String value, int maximum, String source) {
			try {
				int prefixLength = Integer.parseInt(value);
				if (prefixLength < 0 || prefixLength > maximum) {
					throw new IllegalArgumentException("Una direccion de proxy confiable no es valida: " + source);
				}
				return prefixLength;
			}
			catch (NumberFormatException exception) {
				throw new IllegalArgumentException("Una direccion de proxy confiable no es valida: " + source, exception);
			}
		}
	}
}
