package com.agrocenter.bff.exception;

import org.springframework.http.HttpStatus;

public class DownstreamException extends RuntimeException {

    private final HttpStatus status;
    private final String code;
    private final String service;

    public DownstreamException(HttpStatus status, String code, String message, String service) {
        super(message);
        this.status = status;
        this.code = code;
        this.service = service;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getService() {
        return service;
    }

    public static DownstreamException unavailable(String service) {
        return new DownstreamException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "DOWNSTREAM_UNAVAILABLE",
                "El servicio " + service + " no esta disponible temporalmente",
                service
        );
    }

    public static DownstreamException badGateway(String service) {
        return new DownstreamException(
                HttpStatus.BAD_GATEWAY,
                "BAD_GATEWAY",
                "El servicio " + service + " devolvio una respuesta no valida",
                service
        );
    }
}
