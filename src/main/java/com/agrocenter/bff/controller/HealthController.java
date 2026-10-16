package com.agrocenter.bff.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping({"/api/bff/health", "/api/health"})
@Tag(name = "Health", description = "Verificación de disponibilidad y salud del BFF")
public class HealthController {

    @GetMapping
    @Operation(summary = "Comprobar estado del BFF", description = "Retorna el estado de disponibilidad del BFF para monitoreo externo")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }
}
