package com.tecsup.delivery.entrega.dto;

import com.tecsup.delivery.entrega.model.enums.EstadoEntrega;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EntregaResponse(
        Long id,
        Long idCliente,
        Long idRepartidor,
        String direccion,
        LocalDateTime fecha,
        BigDecimal costo,
        EstadoEntrega estado) {
}
