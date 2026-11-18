
package com.ud.cobranzas.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.springframework.dao.DataIntegrityViolationException;

import com.ud.cobranzas.model.dto.PagoRequestDto;
import com.ud.cobranzas.model.entity.CobradorEntity;
import com.ud.cobranzas.model.entity.CreditoEntity;
import com.ud.cobranzas.model.entity.CuotaEntity;
import com.ud.cobranzas.model.entity.GestionCobroEntity;
import com.ud.cobranzas.model.entity.PagoEntity;
import com.ud.cobranzas.repository.CuotaRepository;
import com.ud.cobranzas.repository.GestionCobroRepository;
import com.ud.cobranzas.repository.PagoRepository;

class PagoServiceTest {

    private final PagoRepository pagoRepository =
            mock(PagoRepository.class);

    private final CuotaRepository cuotaRepository =
            mock(CuotaRepository.class);

    private final GestionCobroRepository gestionRepository =
            mock(GestionCobroRepository.class);

    private final PagoService servicio = new PagoService(
            pagoRepository,
            cuotaRepository,
            gestionRepository
    );

    // Preparar una cuota simulada
    private CuotaEntity prepararCuota(
            String estadoCredito,
            String estadoCuota,
            String valor,
            String pagadoAnterior) {

        CobradorEntity cobrador = CobradorEntity.builder()
                .id(7)
                .build();

        CreditoEntity credito = CreditoEntity.builder()
                .id(10)
                .cobrador(cobrador)
                .estado(estadoCredito)
                .build();

        CuotaEntity cuota = CuotaEntity.builder()
                .id(15)
                .credito(credito)
                .montoCuota(new BigDecimal(valor))
                .estado(estadoCuota)
                .build();

        when(cuotaRepository.findByIdForUpdate(15))
                .thenReturn(Optional.of(cuota));

        when(pagoRepository.calcularTotalPagadoPorCuota(15))
                .thenReturn(new BigDecimal(pagadoAnterior));

        return cuota;
    }

    // Crear una solicitud simulada
    private PagoRequestDto solicitud(String monto) {

        PagoRequestDto request = new PagoRequestDto();

        request.setCuotaId(15);
        request.setMonto(new BigDecimal(monto));
        request.setTipoGestion("VISITA");
        request.setObservaciones("Pago de prueba");

        return request;
    }

    // Simular que PostgreSQL recibe una gestión registrada
    private void simularGestionGuardada() {

        when(gestionRepository.saveAndFlush(
                any(GestionCobroEntity.class)
        )).thenAnswer(invocacion -> {

            GestionCobroEntity gestion =
                    invocacion.getArgument(0);

            gestion.setId(30);

            return gestion;
        });
    }

    // Simular el resultado de la función SQL
    private void simularPagoRegistrado(
            String monto,
            boolean pagoCompleto) {

        when(pagoRepository.ejecutarPagoSQL(
                eq(15),
                eq(30),
                eq(new BigDecimal(monto)),
                eq(pagoCompleto)
        )).thenReturn(101);

        CuotaEntity cuota = CuotaEntity.builder()
                .id(15)
                .build();

        PagoEntity pago = PagoEntity.builder()
                .id(101)
                .cuota(cuota)
                .monto(new BigDecimal(monto))
                .fechaPago(LocalDateTime.now())
                .build();

        when(pagoRepository.findById(101))
                .thenReturn(Optional.of(pago));
    }

    // ==================================================
    // PRUEBA 1: PAGO COMPLETO
    // ==================================================

    @Test
    void registraPagoCompletoMedianteFuncionSQL() {

        prepararCuota(
                "AL_DIA",
                "PENDIENTE",
                "100000.00",
                "40000.00"
        );

        simularGestionGuardada();
        simularPagoRegistrado("60000.00", true);

        PagoEntity resultado =
                servicio.registrarPagoPorCobrador(
                        7,
                        solicitud("60000.00")
                );

        assertEquals(101, resultado.getId());

        verify(pagoRepository).ejecutarPagoSQL(
                15,
                30,
                new BigDecimal("60000.00"),
                true
        );

        verify(gestionRepository, times(1))
                .saveAndFlush(any(GestionCobroEntity.class));

        verify(gestionRepository, never())
                .save(any(GestionCobroEntity.class));
    }

    // ==================================================
    // PRUEBA 2: PAGO PARCIAL
    // ==================================================

    @Test
    void registraPagoParcialSinMarcarCuotaPagada() {

        prepararCuota(
                "AL_DIA",
                "PENDIENTE",
                "100000.00",
                "0.00"
        );

        simularGestionGuardada();
        simularPagoRegistrado("30000.00", false);

        PagoEntity resultado =
                servicio.registrarPagoPorCobrador(
                        7,
                        solicitud("30000.00")
                );

        assertEquals(101, resultado.getId());

        verify(pagoRepository).ejecutarPagoSQL(
                15,
                30,
                new BigDecimal("30000.00"),
                false
        );
    }

    // ==================================================
    // PRUEBA 3: PAGO MAYOR AL SALDO
    // ==================================================

    @Test
    void rechazaPagoSuperiorAlSaldo() {

        prepararCuota(
                "AL_DIA",
                "PENDIENTE",
                "100000.00",
                "80000.00"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> servicio.registrarPagoPorCobrador(
                        7,
                        solicitud("30000.00")
                )
        );

        verifyNoInteractions(gestionRepository);

        verify(pagoRepository, never()).ejecutarPagoSQL(
                anyInt(),
                anyInt(),
                any(BigDecimal.class),
                anyBoolean()
        );
    }

    // ==================================================
    // PRUEBA 4: COBRADOR NO AUTORIZADO
    // ==================================================

    @Test
    void rechazaPagoDeOtroCobrador() {

        prepararCuota(
                "AL_DIA",
                "PENDIENTE",
                "100000.00",
                "0.00"
        );

        assertThrows(
                SecurityException.class,
                () -> servicio.registrarPagoPorCobrador(
                        9,
                        solicitud("10000.00")
                )
        );

        verifyNoInteractions(gestionRepository);

        verify(pagoRepository, never()).ejecutarPagoSQL(
                anyInt(),
                anyInt(),
                any(BigDecimal.class),
                anyBoolean()
        );
    }

    // ==================================================
    // PRUEBA 5: CUOTA YA PAGADA
    // ==================================================

    @Test
    void rechazaPagoSobreCuotaPagada() {

        prepararCuota(
                "AL_DIA",
                "PAGADA",
                "100000.00",
                "100000.00"
        );

        assertThrows(
                IllegalStateException.class,
                () -> servicio.registrarPagoPorCobrador(
                        7,
                        solicitud("10000.00")
                )
        );

        verifyNoInteractions(gestionRepository);

        verify(pagoRepository, never()).ejecutarPagoSQL(
                anyInt(),
                anyInt(),
                any(BigDecimal.class),
                anyBoolean()
        );
    }

    // ==================================================
    // PRUEBA 6: MONTO INVÁLIDO
    // ==================================================

    @Test
    void rechazaPagoDeMontoCero() {

        assertThrows(
                IllegalArgumentException.class,
                () -> servicio.registrarPagoPorCobrador(
                        7,
                        solicitud("0.00")
                )
        );

        verifyNoInteractions(
                pagoRepository,
                cuotaRepository,
                gestionRepository
        );
    }

    // ==================================================
    // PRUEBA 7: ERROR DE LA FUNCIÓN SQL
    // ==================================================

    @Test
    void propagaErrorDeLaFuncionSQL() {

        prepararCuota(
                "AL_DIA",
                "PENDIENTE",
                "100000.00",
                "0.00"
        );

        simularGestionGuardada();

        when(pagoRepository.ejecutarPagoSQL(
                15,
                30,
                new BigDecimal("50000.00"),
                false
        )).thenThrow(
                new DataIntegrityViolationException(
                        "Error simulado en PostgreSQL"
                )
        );

        assertThrows(
                DataIntegrityViolationException.class,
                () -> servicio.registrarPagoPorCobrador(
                        7,
                        solicitud("50000.00")
                )
        );

        verify(pagoRepository).ejecutarPagoSQL(
                15,
                30,
                new BigDecimal("50000.00"),
                false
        );

        verify(pagoRepository, never())
                .findById(anyInt());
    }
}
