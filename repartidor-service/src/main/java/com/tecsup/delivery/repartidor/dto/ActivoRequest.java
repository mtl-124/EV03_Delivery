package com.tecsup.delivery.repartidor.dto;

import jakarta.validation.constraints.NotNull;

public record ActivoRequest(
        @NotNull(message = "no debe ser nulo")
        Boolean activo) {
}
