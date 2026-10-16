package com.agrocenter.bff.controller;

import com.agrocenter.bff.dto.messaging.EventosTrazabilidadResponse;
import com.agrocenter.bff.dto.messaging.RoutingRuleRequest;
import com.agrocenter.bff.dto.messaging.RoutingRuleResponse;
import com.agrocenter.bff.dto.messaging.TopicStatusResponse;
import com.agrocenter.bff.service.MessagingQueryService;
import com.agrocenter.bff.service.ReglasEncaminamientoService;
import com.agrocenter.bff.service.TopicAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/bff/admin/mensajeria")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Administracion de Mensajeria", description = "Gestion de topicos, reglas de encaminamiento y trazas de eventos")
public class MessagingAdminController {

    private final TopicAdminService topicAdminService;
    private final ReglasEncaminamientoService reglasService;
    private final MessagingQueryService messagingQueryService;

    public MessagingAdminController(
            TopicAdminService topicAdminService,
            ReglasEncaminamientoService reglasService,
            MessagingQueryService messagingQueryService
    ) {
        this.topicAdminService = topicAdminService;
        this.reglasService = reglasService;
        this.messagingQueryService = messagingQueryService;
    }

    @GetMapping("/topicos")
    @Operation(summary = "Consultar el estado y las particiones de los topicos de Kafka")
    public List<TopicStatusResponse> topicos() {
        return topicAdminService.describirTopicos();
    }

    @GetMapping("/reglas")
    @Operation(summary = "Listar las reglas de encaminamiento registradas")
    public List<RoutingRuleResponse> reglas() {
        return reglasService.listar();
    }

    @PostMapping("/reglas")
    @Operation(summary = "Registrar una nueva regla de distribucion de eventos")
    public ResponseEntity<RoutingRuleResponse> crearRegla(@Valid @RequestBody RoutingRuleRequest request) {
        RoutingRuleResponse regla = reglasService.crear(request);
        return ResponseEntity
                .created(URI.create("/api/bff/admin/mensajeria/reglas/" + regla.id()))
                .body(regla);
    }

    @DeleteMapping("/reglas/{id}")
    @Operation(summary = "Eliminar una regla de encaminamiento")
    public ResponseEntity<Void> eliminarRegla(@PathVariable String id) {
        return reglasService.eliminar(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/eventos")
    @Operation(summary = "Consultar la trazabilidad de eventos asincronos procesados y fallidos")
    public EventosTrazabilidadResponse eventos(
            @RequestParam(defaultValue = "50") @Min(1) @Max(200) int limite
    ) {
        return messagingQueryService.eventos(limite);
    }
}
