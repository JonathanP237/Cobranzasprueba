package com.ud.cobranzas.controller;

import com.ud.cobranzas.model.entity.CobradorEntity;
import com.ud.cobranzas.repository.CuotaRepository;
import com.ud.cobranzas.repository.GestionCobroRepository;
import com.ud.cobranzas.service.CobradorConsultaService;
import com.ud.cobranzas.service.PagoService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class CobradorControllerSeguridadTest {
    private final CobradorConsultaService consultas = mock(CobradorConsultaService.class);
    private final CobradorController controller = new CobradorController(
            consultas, mock(PagoService.class), mock(CuotaRepository.class),
            mock(GestionCobroRepository.class));

    @Test
    void impideConsultarCarteraAjena() {
        CobradorEntity autenticado = CobradorEntity.builder().id(7).activo(true).build();
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.listarCreditosDelCobrador(9, autenticado));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verifyNoInteractions(consultas);
    }

    @Test
    void exigePrincipalAutenticado() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.listarCreditosDelCobrador(7, null));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        verifyNoInteractions(consultas);
    }
}
