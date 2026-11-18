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

        private final ReportesService servicio = mock(ReportesService.class);

        private final ReportesController controller = new ReportesController(servicio);

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
                                () -> controller.historialRecaudos(null));

                assertEquals(
                                HttpStatus.UNAUTHORIZED,
                                error.getStatusCode());

                verifyNoInteractions(servicio);
        }

        @Test
        void consultaCreditosSobrePromedioDelCobradorAutenticado() {

                CobradorEntity cobrador = CobradorEntity.builder()
                                .id(7)
                                .activo(true)
                                .build();

                when(servicio.obtenerCreditosSobrePromedio(7))
                                .thenReturn(List.of());

                var respuesta = controller.creditosSobrePromedio(cobrador);

                assertEquals(HttpStatus.OK, respuesta.getStatusCode());
                assertEquals(0, respuesta.getBody().size());

                verify(servicio).obtenerCreditosSobrePromedio(7);
                verifyNoMoreInteractions(servicio);
        }

        @Test
        void rechazaPromedioSinAutenticacion() {

                ResponseStatusException error = assertThrows(
                                ResponseStatusException.class,
                                () -> controller.creditosSobrePromedio(null));

                assertEquals(
                                HttpStatus.UNAUTHORIZED,
                                error.getStatusCode());

                verifyNoInteractions(servicio);
        }

        @Test
        void consultaClientesMorososDelCobradorAutenticado() {

                CobradorEntity cobrador = CobradorEntity.builder()
                                .id(7)
                                .activo(true)
                                .build();

                when(servicio.obtenerClientesMorosos(7))
                                .thenReturn(List.of());

                var respuesta = controller.clientesMorosos(cobrador);

                assertEquals(HttpStatus.OK, respuesta.getStatusCode());
                assertEquals(0, respuesta.getBody().size());

                verify(servicio).obtenerClientesMorosos(7);
                verifyNoMoreInteractions(servicio);
        }

        @Test
        void rechazaClientesMorososSinAutenticacion() {

                ResponseStatusException error = assertThrows(
                                ResponseStatusException.class,
                                () -> controller.clientesMorosos(null));

                assertEquals(
                                HttpStatus.UNAUTHORIZED,
                                error.getStatusCode());

                verifyNoInteractions(servicio);
        }

        @Test
        void consultaCuotasVencidasDelCobradorAutenticado() {

                CobradorEntity cobrador = CobradorEntity.builder()
                                .id(7)
                                .activo(true)
                                .build();

                when(servicio.obtenerCuotasVencidas(7))
                                .thenReturn(List.of());

                var respuesta = controller.cuotasVencidas(cobrador);

                assertEquals(
                                HttpStatus.OK,
                                respuesta.getStatusCode());

                assertEquals(0, respuesta.getBody().size());

                verify(servicio).obtenerCuotasVencidas(7);
                verifyNoMoreInteractions(servicio);
        }

        @Test
        void rechazaCuotasVencidasSinAutenticacion() {

                ResponseStatusException error = assertThrows(
                                ResponseStatusException.class,
                                () -> controller.cuotasVencidas(null));

                assertEquals(
                                HttpStatus.UNAUTHORIZED,
                                error.getStatusCode());

                verifyNoInteractions(servicio);
        }

        @Test
        void consultaResumenMaterializadoDelCobradorAutenticado() {

                CobradorEntity cobrador = CobradorEntity.builder()
                                .id(7)
                                .activo(true)
                                .build();

                when(servicio.obtenerResumenMaterializado(7))
                                .thenReturn(List.of());

                var respuesta = controller.resumenMaterializado(cobrador);

                assertEquals(HttpStatus.OK, respuesta.getStatusCode());
                assertEquals(0, respuesta.getBody().size());

                verify(servicio).obtenerResumenMaterializado(7);
                verifyNoMoreInteractions(servicio);
        }

        @Test
        void rechazaResumenMaterializadoSinAutenticacion() {

                ResponseStatusException error = assertThrows(
                                ResponseStatusException.class,
                                () -> controller.resumenMaterializado(null));

                assertEquals(
                                HttpStatus.UNAUTHORIZED,
                                error.getStatusCode());

                verifyNoInteractions(servicio);
        }
}