package com.agrocenter.bff.dto.consultas;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ConsultaB2BRequest(
        @Size(max = 100) String skuProducto,
        @Size(max = 255) String nombreProducto,
        @Positive Integer cantidad,
        @NotBlank @Size(min = 2, max = 150) String nombreCliente,
        @NotBlank @Email @Size(max = 150) String correoContacto,
        @NotBlank @Size(min = 10, max = 4000) String mensaje,
        @NotBlank @Size(max = 50) String origen
) {
}
