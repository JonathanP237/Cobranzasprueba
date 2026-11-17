package com.ud.cobranzas.controller;

import com.ud.cobranzas.model.entity.CobradorEntity;
import com.ud.cobranzas.model.entity.CreditoEntity;
import com.ud.cobranzas.repository.ClienteRepository;
import com.ud.cobranzas.repository.CobradorRepository;
import com.ud.cobranzas.repository.CreditoRepository;
import com.ud.cobranzas.repository.CuotaRepository;
import com.ud.cobranzas.repository.GestionCobroRepository;
import com.ud.cobranzas.service.CreditoService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class MainControllerSeguridadTest {
    private final CreditoRepository creditos = mock(CreditoRepository.class);
    private final CuotaRepository cuotas = mock(CuotaRepository.class);
    private final GestionCobroRepository gestiones = mock(GestionCobroRepository.class);
    private final MainController controller = new MainController(
            mock(ClienteRepository.class), mock(CobradorRepository.class),
            creditos, cuotas, gestiones, mock(CreditoService.class));

    private static CobradorEntity cobrador(int id) {
        return CobradorEntity.builder().id(id).activo(true).build();
    }

    private void simularCreditoAjeno() {
        CreditoEntity ajeno = CreditoEntity.builder()
                .id(18).cobrador(cobrador(99)).build();
        when(creditos.findById(18)).thenReturn(Optional.of(ajeno));
    }

    @Test
    void listarCreditosSoloConsultaPropios() {
        when(creditos.findByCobradorId(7)).thenReturn(List.of());
        assertEquals(0, controller.listarCreditos(cobrador(7)).getBody().size());
        verify(creditos).findByCobradorId(7);
        verify(creditos, never()).findAll();
    }

    @Test
    void directorioCobradoresNoExponeOtrasCuentas() {
        var body = controller.listarCobradoresActivos(cobrador(7)).getBody();
        assertEquals(1, body.size());
        assertEquals(7, body.get(0).id());
    }

    @Test
    void noDevuelveCuotasDeCreditoAjeno() {
        simularCreditoAjeno();
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.listarCuotasPorCredito(18, cobrador(7)));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verifyNoInteractions(cuotas);
    }

    @Test
    void noDevuelveGestionesDeCreditoAjeno() {
        simularCreditoAjeno();
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.listarGestiones(18, cobrador(7)));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verifyNoInteractions(gestiones);
    }
}
