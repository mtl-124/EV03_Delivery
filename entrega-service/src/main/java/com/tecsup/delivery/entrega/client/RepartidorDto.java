package com.tecsup.delivery.entrega.client;

public record RepartidorDto(
        Long id,
        String nombre,
        String apellido,
        String telefono,
        String vehiculo,
        Boolean activo) {
}
