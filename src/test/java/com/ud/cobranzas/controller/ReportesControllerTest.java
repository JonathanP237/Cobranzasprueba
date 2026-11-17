package com.ud.cobranzas.controller;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.ud.cobranzas.model.entity.CobradorEntity;
import com.ud.cobranzas.service.ReportesService;

class ReportesControllerTest {

    private final ReportesService servicio =
            mock(ReportesService.class);

    private final ReportesController controller =
            new ReportesController(servicio);

    @Test
    void consultaRecaudosDelCobradorAutenticado() {

        CobradorEntity cobrador = CobradorEntity.builder()
                .id(7)
                .activo(true)
                .build();

        when(servicio.obtenerRecaudos(7))
                .thenReturn(List.of());

        var respuesta = controller.historialRecaudos(cobrador);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());

        verify(servicio).obtenerRecaudos(7);
        verifyNoMoreInteractions(servicio);
    }

    @Test
    void impideConsultaSinAutenticacion() {

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> controller.historialRecaudos(null)
        );

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                error.getStatusCode()
        );

        verifyNoInteractions(servicio);
    }
}