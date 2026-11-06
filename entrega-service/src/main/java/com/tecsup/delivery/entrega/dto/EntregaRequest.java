package com.tecsup.delivery.entrega.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record EntregaRequest(
        @NotNull(message = "no debe ser nulo")
        Long idCliente,
        @NotNull(message = "no debe ser nulo")
        Long idRepartidor,
        @NotBlank(message = "no debe estar vacío")
        String direccion,
        @NotNull(message = "no debe ser nulo")
        @PositiveOrZero(message = "debe ser mayor o igual que 0")
        BigDecimal costo) {
}
