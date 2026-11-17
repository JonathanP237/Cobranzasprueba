
package com.ud.cobranzas.service;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.ud.cobranzas.model.dto.ResumenCarteraDto;
import com.ud.cobranzas.repository.ReportesRepository;

class ReportesServiceTest {

    private final ReportesRepository repositorio =
            mock(ReportesRepository.class);

    private final ReportesService servicio =
            new ReportesService(repositorio);

    @Test
    void obtieneTotalesDeCarteraCorrectamente() {

        ReportesRepository.CarteraFila fila =
                mock(ReportesRepository.CarteraFila.class);

        when(fila.getCobradorId()).thenReturn(3);
        when(fila.getNombreCobrador()).thenReturn("Cobrador prueba");
        when(fila.getTotalCreditos()).thenReturn(2L);
        when(fila.getTotalCapital())
                .thenReturn(new BigDecimal("1000000.00"));
        when(fila.getTotalInteres())
                .thenReturn(new BigDecimal("200000.00"));
        when(fila.getCarteraBruta())
                .thenReturn(new BigDecimal("1200000.00"));

        when(repositorio.obtenerResumenCartera(3))
                .thenReturn(List.of(fila));

        ResumenCarteraDto resultado =
                servicio.obtenerResumenCartera(3);

        assertEquals(3, resultado.cobradorId());
        assertEquals(2L, resultado.totalCreditos());
        assertEquals(
                0,
                new BigDecimal("1200000.00")
                        .compareTo(resultado.carteraBruta())
        );

        verify(repositorio).obtenerResumenCartera(3);
    }

    @Test
    void mantieneCobradorSinCreditosEnElResumen() {

        ReportesRepository.CarteraFila fila =
                mock(ReportesRepository.CarteraFila.class);

        when(fila.getCobradorId()).thenReturn(8);
        when(fila.getNombreCobrador()).thenReturn("Sin créditos");
        when(fila.getTotalCreditos()).thenReturn(0L);
        when(fila.getTotalCapital()).thenReturn(BigDecimal.ZERO);
        when(fila.getTotalInteres()).thenReturn(BigDecimal.ZERO);
        when(fila.getCarteraBruta()).thenReturn(BigDecimal.ZERO);

        when(repositorio.obtenerResumenCartera(8))
                .thenReturn(List.of(fila));

        ResumenCarteraDto resultado =
                servicio.obtenerResumenCartera(8);

        assertEquals(8, resultado.cobradorId());
        assertEquals(0L, resultado.totalCreditos());
        assertEquals(
                0,
                BigDecimal.ZERO.compareTo(resultado.carteraBruta())
        );

        verify(repositorio).obtenerResumenCartera(8);
    }

    @Test
    void devuelve404CuandoNoExisteResumen() {

        when(repositorio.obtenerResumenCartera(999))
                .thenReturn(List.of());

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> servicio.obtenerResumenCartera(999)
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                error.getStatusCode()
        );

        verify(repositorio).obtenerResumenCartera(999);
    }
}
