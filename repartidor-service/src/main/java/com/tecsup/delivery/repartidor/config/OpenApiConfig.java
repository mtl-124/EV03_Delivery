package com.tecsup.delivery.repartidor.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI repartidorOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("repartidor-service")
                        .description("API de gestión de repartidores")
                        .version("1.0.0"));
    }
}
