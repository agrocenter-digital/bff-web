package com.agrocenter.bff.dto.common;

import java.util.List;

public record PageResponse<T>(
        List<T> contenido,
        int pagina,
        int tamanio,
        long totalElementos,
        int totalPaginas
) {
}
