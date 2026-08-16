package com.flower_details.features.order.infrastructure.numbering;

import com.flower_details.features.order.application.service.OrderNumberGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

@Component
@RequiredArgsConstructor
class UuidOrderNumberGenerator implements OrderNumberGenerator {

	private final Clock clock;

	@Override
	public String next() {
		String date = LocalDate.now(clock).toString().replace("-", "");
		String suffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
		return "FD-" + date + "-" + suffix;
	}
}
