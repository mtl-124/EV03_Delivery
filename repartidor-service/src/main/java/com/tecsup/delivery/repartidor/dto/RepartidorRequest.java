package com.tecsup.delivery.repartidor.dto;

import com.tecsup.delivery.repartidor.model.enums.TipoVehiculo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RepartidorRequest(
        @NotBlank(message = "no debe estar vacío")
        String nombre,
        @NotBlank(message = "no debe estar vacío")
        String apellido,
        @NotBlank(message = "no debe estar vacío")
        String telefono,
        @NotNull(message = "no debe ser nulo")
        TipoVehiculo vehiculo,
        Boolean activo) {
}
