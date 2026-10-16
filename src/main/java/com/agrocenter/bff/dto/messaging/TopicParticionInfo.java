package com.agrocenter.bff.dto.messaging;

import java.util.List;

public record TopicParticionInfo(
        int particion,
        Integer lider,
        List<Integer> replicas,
        List<Integer> sincronizadas,
        Long offsetFinal
) {
}
