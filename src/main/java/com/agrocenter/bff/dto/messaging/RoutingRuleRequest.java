package com.agrocenter.bff.dto.messaging;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RoutingRuleRequest(
        @NotBlank(message = "El nombre de la regla es obligatorio")
        @Size(max = 80, message = "El nombre no puede superar los 80 caracteres")
        String nombre,

        @NotBlank(message = "El topico de origen es obligatorio")
        @Size(max = 120, message = "El topico de origen no puede superar los 120 caracteres")
        String topicoOrigen,

        @Size(max = 120, message = "El topico de destino no puede superar los 120 caracteres")
        String topicoDestino,

        @Size(max = 200, message = "La condicion no puede superar los 200 caracteres")
        String condicion,

        @Pattern(regexp = "ACTIVA|PAUSADA", message = "El estado debe ser ACTIVA o PAUSADA")
        String estado,

        @Size(max = 300, message = "La descripcion no puede superar los 300 caracteres")
        String descripcion
) {
}
