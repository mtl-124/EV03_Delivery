package com.tecsup.delivery.entrega.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI entregaOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("entrega-service")
                        .description("API de gestión de entregas")
                        .version("1.0.0"));
    }
}
