package com.tecsup.delivery.entrega.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "repartidor-service")
public interface RepartidorClient {

    @GetMapping("/api/repartidores/{id}")
    RepartidorDto obtener(@PathVariable Long id);
}
