package com.ud.cobranzas.service;

import com.ud.cobranzas.model.dto.PagoRequestDto;
import com.ud.cobranzas.model.entity.*;
import com.ud.cobranzas.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PagoService {

    private final PagoRepository pagoRepository;
    private final CuotaRepository cuotaRepository;
    private final GestionCobroRepository gestionCobroRepository;

    @Transactional
    public PagoEntity registrarPagoPorCobrador(Integer cobradorIdLogueado, PagoRequestDto request) {
        if (request.getMonto() == null || request.getMonto().signum() <= 0) {
            throw new IllegalArgumentException("El monto del pago debe ser mayor que cero.");
        }

        CuotaEntity cuota = cuotaRepository.findByIdForUpdate(request.getCuotaId())
                .orElseThrow(() -> new RuntimeException("Cuota no encontrada con ID: " + request.getCuotaId()));

        CreditoEntity credito = cuota.getCredito();

        if (!credito.getCobrador().getId().equals(cobradorIdLogueado)) {
            throw new SecurityException("Acceso denegado: Este crédito no está asignado a este cobrador.");
        }

        if ("PAGADA".equals(cuota.getEstado())) {
            throw new IllegalStateException("La cuota ya está pagada.");
        }

        BigDecimal totalPagado = pagoRepository.calcularTotalPagadoPorCuota(cuota.getId());
        BigDecimal totalConEstePago = totalPagado.add(request.getMonto());
        if (totalConEstePago.compareTo(cuota.getMontoCuota()) > 0) {
            throw new IllegalArgumentException("El pago supera el saldo pendiente de la cuota.");
        }

        GestionCobroEntity gestion = GestionCobroEntity.builder()
                .credito(credito)
                .cuota(cuota)
                .fechaGestion(LocalDateTime.now())
                .tipoGestion(request.getTipoGestion() != null ? request.getTipoGestion() : "VISITA")
                .resultado("PAGO_CONFIRMADO")
                .observaciones(request.getObservaciones() != null ? request.getObservaciones() : "Pago registrado por el cobrador asignado en la app.")
                .build();

        gestionCobroRepository.save(gestion);

        PagoEntity pago = pagoRepository.save(PagoEntity.builder()
                .cuota(cuota)
                .gestionCobro(gestion)
                .monto(request.getMonto())
                .fechaPago(LocalDateTime.now())
                .build());

        cuota.setEstado(totalConEstePago.compareTo(cuota.getMontoCuota()) == 0 ? "PAGADA" : "PENDIENTE");
        cuotaRepository.save(cuota);

        return pago;
    }
}