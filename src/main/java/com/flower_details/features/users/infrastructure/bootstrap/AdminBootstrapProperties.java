package com.flower_details.features.users.infrastructure.bootstrap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public record AdminBootstrapProperties(
		@Value("${app.bootstrap.admin.enabled:false}")
		boolean enabled,

		@Value("${app.bootstrap.admin.names:Administrador}")
		String names,

		@Value("${app.bootstrap.admin.last-names:Flower Details}")
		String lastNames,

		@Value("${app.bootstrap.admin.email:admin@flowerdetails.local}")
		String email,

		@Value("${app.bootstrap.admin.password:Admin12345}")
		String password
) {
}
