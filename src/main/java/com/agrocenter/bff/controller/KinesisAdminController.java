package com.agrocenter.bff.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.agrocenter.bff.client.ClientCallExecutor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

/** Proxy limitado a operaciones conocidas; la autorización ADMIN se aplica en ambos servicios. */
@RestController
@RequestMapping("/api/bff/admin/kinesis/streams")
public class KinesisAdminController {
    private final RestClient client;
    private final ClientCallExecutor executor;
    public KinesisAdminController(@Qualifier("kinesisAdminRestClient") RestClient client, ClientCallExecutor executor) { this.client = client; this.executor = executor; }
    @GetMapping public JsonNode list() { return executor.execute("ms-kinesis-admin", "listar streams", () -> client.get().uri("/api/kinesis-admin/streams").retrieve().body(JsonNode.class)); }
    @GetMapping("/{name}") public JsonNode detail(@PathVariable String name) { return executor.execute("ms-kinesis-admin", "consultar stream", () -> client.get().uri("/api/kinesis-admin/streams/{name}", name).retrieve().body(JsonNode.class)); }
    @PostMapping public ResponseEntity<JsonNode> create(@RequestBody JsonNode body) {
        return ResponseEntity.accepted().body(executor.execute("ms-kinesis-admin", "crear stream", () -> client.post().uri("/api/kinesis-admin/streams").body(body).retrieve().body(JsonNode.class)));
    }
    @PatchMapping("/{name}/{operation:retencion|shards}") public ResponseEntity<JsonNode> change(@PathVariable String name, @PathVariable String operation, @RequestBody JsonNode body) {
        return ResponseEntity.accepted().body(executor.execute("ms-kinesis-admin", "modificar stream", () -> client.patch().uri("/api/kinesis-admin/streams/{name}/{operation}", name, operation).body(body).retrieve().body(JsonNode.class)));
    }
    @DeleteMapping("/{name}") public ResponseEntity<JsonNode> delete(@PathVariable String name, @RequestParam String confirmacion) {
        return ResponseEntity.accepted().body(executor.execute("ms-kinesis-admin", "eliminar stream", () -> client.delete().uri(b -> b.path("/api/kinesis-admin/streams/{name}").queryParam("confirmacion", confirmacion).build(name)).retrieve().body(JsonNode.class)));
    }
}
