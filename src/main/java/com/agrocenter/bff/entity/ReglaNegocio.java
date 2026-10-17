package com.agrocenter.bff.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "reglas_distribucion")
public class ReglaNegocio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(name = "topico_origen", nullable = false, length = 120)
    private String topicoOrigen;

    @Column(name = "topico_destino", length = 120)
    private String topicoDestino;

    @Column(length = 200)
    private String condicion;

    @Column(length = 300)
    private String descripcion;

    @Column(nullable = false, length = 20)
    private String estado = "ACTIVA";

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion;

    protected ReglaNegocio() {
    }

    public ReglaNegocio(
            String nombre,
            String topicoOrigen,
            String topicoDestino,
            String condicion,
            String descripcion,
            String estado
    ) {
        this.nombre = nombre;
        this.topicoOrigen = topicoOrigen;
        this.topicoDestino = topicoDestino;
        this.condicion = condicion;
        this.descripcion = descripcion;
        this.estado = estado;
    }

    @PrePersist
    void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = Instant.now();
        }
        if (estado == null || estado.isBlank()) {
            estado = "ACTIVA";
        }
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTopicoOrigen() {
        return topicoOrigen;
    }

    public void setTopicoOrigen(String topicoOrigen) {
        this.topicoOrigen = topicoOrigen;
    }

    public String getTopicoDestino() {
        return topicoDestino;
    }

    public void setTopicoDestino(String topicoDestino) {
        this.topicoDestino = topicoDestino;
    }

    public String getCondicion() {
        return condicion;
    }

    public void setCondicion(String condicion) {
        this.condicion = condicion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }
}
