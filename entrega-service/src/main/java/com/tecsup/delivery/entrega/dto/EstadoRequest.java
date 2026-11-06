package com.tecsup.delivery.entrega.dto;

import com.tecsup.delivery.entrega.model.enums.EstadoEntrega;
import jakarta.validation.constraints.NotNull;

public record EstadoRequest(
        @NotNull(message = "no debe ser nulo")
        EstadoEntrega estado) {
}
