package com.agrocenter.bff.controller;

import com.agrocenter.bff.service.MessagingQueryService;
import com.agrocenter.bff.service.ReglasEncaminamientoService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class TopicosRetiradosTest {
    @Test
    void rutaAnteriorInformaRetiroSinConsultarBroker() {
        var controller = new MessagingAdminController(
                mock(ReglasEncaminamientoService.class), mock(MessagingQueryService.class));
        assertThat(controller.topicos().getStatusCode()).isEqualTo(HttpStatus.GONE);
    }
}
