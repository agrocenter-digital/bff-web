package com.agrocenter.bff.service;

import com.agrocenter.bff.config.MessagingProperties;
import com.agrocenter.bff.dto.messaging.RoutingRuleRequest;
import com.agrocenter.bff.dto.messaging.RoutingRuleResponse;
import com.agrocenter.bff.entity.ReglaNegocio;
import com.agrocenter.bff.repository.ReglaNegocioRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReglasEncaminamientoService {

    private final ReglaNegocioRepository repository;
    private final MessagingProperties properties;

    public ReglasEncaminamientoService(ReglaNegocioRepository repository, MessagingProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    @Transactional(readOnly = true)
    public List<RoutingRuleResponse> listar() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "fechaCreacion"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public RoutingRuleResponse crear(RoutingRuleRequest request) {
        String origen = request.topicoOrigen().trim();
        if (!properties.topicos().contains(origen)) {
            throw new IllegalArgumentException(
                    "El topico de origen '" + origen + "' no es un topico conocido del sistema"
            );
        }

        String estado = request.estado() == null || request.estado().isBlank() ? "ACTIVA" : request.estado();
        ReglaNegocio regla = new ReglaNegocio(
                request.nombre().trim(),
                origen,
                normalizar(request.topicoDestino()),
                normalizar(request.condicion()),
                normalizar(request.descripcion()),
                estado
        );
        return toResponse(repository.save(regla));
    }

    @Transactional
    public boolean eliminar(String id) {
        Long identificador = parsearId(id);
        if (identificador == null || !repository.existsById(identificador)) {
            return false;
        }
        repository.deleteById(identificador);
        return true;
    }

    private Long parsearId(String id) {
        try {
            return Long.valueOf(id);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private RoutingRuleResponse toResponse(ReglaNegocio regla) {
        return new RoutingRuleResponse(
                String.valueOf(regla.getId()),
                regla.getNombre(),
                regla.getTopicoOrigen(),
                regla.getTopicoDestino(),
                regla.getCondicion(),
                regla.getEstado(),
                regla.getDescripcion(),
                regla.getFechaCreacion()
        );
    }

    private String normalizar(String valor) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.trim();
        return limpio.isEmpty() ? null : limpio;
    }
}
