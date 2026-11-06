package com.tecsup.delivery.repartidor.dto;

import com.tecsup.delivery.repartidor.model.enums.TipoVehiculo;

public record RepartidorResponse(
        Long id,
        String nombre,
        String apellido,
        String telefono,
        TipoVehiculo vehiculo,
        Boolean activo) {
}
