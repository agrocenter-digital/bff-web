package com.agrocenter.bff.dto.messaging;

import java.util.List;

public record TopicStatusResponse(
        String nombre,
        String estado,
        int totalParticiones,
        int factorReplicacion,
        long mensajesTotales,
        String detalle,
        List<TopicParticionInfo> particiones
) {
}
