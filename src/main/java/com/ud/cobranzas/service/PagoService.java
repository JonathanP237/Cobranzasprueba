package com.ud.cobranzas.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ud.cobranzas.model.dto.PagoRequestDto;
import com.ud.cobranzas.model.entity.CreditoEntity;
import com.ud.cobranzas.model.entity.CuotaEntity;
import com.ud.cobranzas.model.entity.GestionCobroEntity;
import com.ud.cobranzas.model.entity.PagoEntity;
import com.ud.cobranzas.repository.CuotaRepository;
import com.ud.cobranzas.repository.GestionCobroRepository;
import com.ud.cobranzas.repository.PagoRepository;

import lombok.RequiredArgsConstructor;

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
            throw new SecurityException(
                    "Acceso denegado: Este crédito no está asignado a este cobrador.");
        }

        // Validar que el crédito permita recibir pagos.
        if (!"AL_DIA".equals(credito.getEstado())) {
            throw new IllegalStateException(
                    "No se pueden registrar pagos sobre un crédito en estado "
                            + credito.getEstado());
        }

        // Validar que la cuota aún tenga pagos pendientes.
        if ("PAGADA".equals(cuota.getEstado())) {
            throw new IllegalStateException(
                    "La cuota ya está pagada.");
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
                .observaciones(request.getObservaciones() != null ? request.getObservaciones()
                        : "Pago registrado por el cobrador asignado en la app.")
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