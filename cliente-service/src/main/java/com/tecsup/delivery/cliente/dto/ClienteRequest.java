package com.tecsup.delivery.cliente.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ClienteRequest(
        @NotBlank(message = "no debe estar vacío")
        String nombre,
        @NotBlank(message = "no debe estar vacío")
        String apellido,
        @NotBlank(message = "no debe estar vacío")
        @Email(message = "debe ser un email válido")
        String email,
        @NotBlank(message = "no debe estar vacío")
        @Pattern(regexp = "\\d{7,15}", message = "debe tener entre 7 y 15 dígitos")
        String telefono,
        @NotBlank(message = "no debe estar vacío")
        String direccion) {
}
