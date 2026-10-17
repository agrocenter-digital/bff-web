package com.agrocenter.bff.dto.consultas;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ActualizarEstadoConsultaRequest(
        @NotBlank @Size(max = 30) String estado
) {
}
