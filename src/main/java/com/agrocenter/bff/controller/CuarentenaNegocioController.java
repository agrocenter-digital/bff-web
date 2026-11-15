package com.agrocenter.bff.controller;

import com.agrocenter.bff.client.CuarentenaNegocioClient;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bff/admin")
public class CuarentenaNegocioController {
    private final CuarentenaNegocioClient client;
    public CuarentenaNegocioController(CuarentenaNegocioClient client) { this.client = client; }

    @GetMapping("/{servicio:inventario|compras}/cuarentena")
    public JsonNode listar(@PathVariable String servicio, @RequestParam(defaultValue = "0") int pagina,
                           @RequestParam(defaultValue = "25") int tamano,
                           @RequestParam(defaultValue = "true") boolean pendientes) {
        if (pagina < 0 || pagina > 10000 || tamano < 1 || tamano > 100) throw new IllegalArgumentException("Paginacion invalida");
        return client.consultar("inventario".equals(servicio), pagina, tamano, pendientes);
    }
}
