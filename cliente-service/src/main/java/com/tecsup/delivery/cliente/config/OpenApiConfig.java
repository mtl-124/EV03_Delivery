package com.tecsup.delivery.cliente.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI clienteOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("cliente-service")
                        .description("API de gestión de clientes")
                        .version("1.0.0"));
    }
}
