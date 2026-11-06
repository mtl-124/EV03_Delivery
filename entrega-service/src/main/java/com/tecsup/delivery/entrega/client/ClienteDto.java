package com.tecsup.delivery.entrega.client;

public record ClienteDto(
        Long id,
        String nombre,
        String apellido,
        String email,
        String telefono,
        String direccion) {
}
