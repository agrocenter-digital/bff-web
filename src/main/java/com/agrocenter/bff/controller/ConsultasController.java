package com.agrocenter.bff.controller;

import com.agrocenter.bff.client.ConsultasClient;
import com.agrocenter.bff.dto.consultas.ActualizarEstadoConsultaRequest;
import com.agrocenter.bff.dto.consultas.ConsultaB2BRequest;
import com.agrocenter.bff.dto.consultas.ConsultaB2BResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/bff")
public class ConsultasController {

    private final ConsultasClient consultasClient;

    public ConsultasController(ConsultasClient consultasClient) {
        this.consultasClient = consultasClient;
    }

    @PostMapping({"/consultas", "/consultas/"})
    public ResponseEntity<ConsultaB2BResponse> crear(@Valid @RequestBody ConsultaB2BRequest request) {
        ConsultaB2BResponse consulta = consultasClient.crear(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(consulta.id())
                .toUri();
        return ResponseEntity.created(location).body(consulta);
    }

    @GetMapping({"/admin/consultas", "/admin/consultas/"})
    @PreAuthorize("hasRole('ADMIN')")
    public List<ConsultaB2BResponse> listar(
            @RequestParam(required = false) @Size(max = 30) String estado
    ) {
        return consultasClient.listar(estado);
    }

    @PatchMapping("/admin/consultas/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ConsultaB2BResponse actualizarEstado(
            @PathVariable @Positive Long id,
            @Valid @RequestBody ActualizarEstadoConsultaRequest request
    ) {
        return consultasClient.actualizarEstado(id, request);
    }
}
