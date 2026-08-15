package com.flower_details.shared.infrastructure.documentation;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class OpenApiConfiguration {

	@Bean
	OpenAPI flowerDetailsOpenApi() {
		return new OpenAPI()
				.info(new Info()
						.title("Flower Details API")
						.description("API para el catalogo, carrito y gestion de pedidos de Flower Details.")
						.version("v1")
						.license(new License().name("Propietario"))
				)
				.components(new Components().addSecuritySchemes(
						"bearerAuth",
						new SecurityScheme()
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")
				));
	}
}
