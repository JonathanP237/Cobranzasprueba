package com.ud.cobranzas.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ud.cobranzas.model.dto.CreditoRequestDto;
import com.ud.cobranzas.model.entity.CreditoEntity;
import com.ud.cobranzas.model.entity.CuotaEntity;
import com.ud.cobranzas.repository.ClienteRepository;
import com.ud.cobranzas.repository.CobradorRepository;
import com.ud.cobranzas.repository.CreditoRepository;
import com.ud.cobranzas.repository.CuotaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreditoService {
    private final ClienteRepository clienteRepository;
    private final CobradorRepository cobradorRepository;
    private final CreditoRepository creditoRepository;
    private final CuotaRepository cuotaRepository;

    @Transactional
    public CreditoEntity crear(CreditoRequestDto request) {
        if (!request.fechaLimite().isAfter(request.fechaInicio())) {
            throw new IllegalArgumentException("La fecha límite debe ser posterior al inicio.");
        }

        calcularTasa(request.fechaInicio(), request.fechaLimite());
        var cliente = clienteRepository.findById(request.clienteId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado."));
        var cobrador = cobradorRepository.findById(request.cobradorId())
                .filter(c -> Boolean.TRUE.equals(c.getActivo()))
                .orElseThrow(() -> new IllegalArgumentException("Cobrador no encontrado o inactivo."));

        CreditoEntity credito = creditoRepository.saveAndFlush(CreditoEntity.builder()
                .cliente(cliente)
                .cobrador(cobrador)
                .monto(request.monto())
                .fechaInicio(request.fechaInicio())
                .fechaLimite(request.fechaLimite())
                .frecuenciaPago(request.frecuenciaPago())
                .estado("AL_DIA")
                .build());

        BigDecimal tasa = creditoRepository.obtenerTasaInteres(credito.getId());
        BigDecimal total = request.monto()
                .add(request.monto().multiply(tasa).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
        List<LocalDate> fechas = generarFechas(request.fechaInicio(), request.fechaLimite(), request.frecuenciaPago());
        BigDecimal base = total.divide(BigDecimal.valueOf(fechas.size()), 2, RoundingMode.HALF_UP);
        BigDecimal asignado = BigDecimal.ZERO;
        List<CuotaEntity> cuotas = new ArrayList<>();

        for (int i = 0; i < fechas.size(); i++) {
            BigDecimal montoCuota;
            if (i == fechas.size() - 1) {
                montoCuota = total.subtract(asignado);
            } else {
                montoCuota = base;
                asignado = asignado.add(base);
            }
            cuotas.add(CuotaEntity.builder()
                    .credito(credito)
                    .numeroCuota(i + 1)
                    .montoCuota(montoCuota)
                    .fechaVencimiento(fechas.get(i))
                    .estado("PENDIENTE")
                    .build());
        }
        cuotaRepository.saveAll(cuotas);
        credito.setTasaInteres(tasa);
        return credito;
    }

    private BigDecimal calcularTasa(LocalDate inicio, LocalDate limite) {
        long dias = ChronoUnit.DAYS.between(inicio, limite);
        if (dias == 7) {
            return BigDecimal.valueOf(5);
        }
        if (dias == 15) {
            return BigDecimal.TEN;
        }
        if (dias >= 28) {
            return BigDecimal.valueOf(20);
        }
        throw new IllegalArgumentException("El plazo debe ser de exactamente 7 días (5%), 15 días (10%) o de un mes o más (20%).");
    }

    private List<LocalDate> generarFechas(LocalDate inicio, LocalDate limite, String frecuencia) {
        List<LocalDate> fechas = new ArrayList<>();
        int periodoDias = switch (frecuencia) {
            case "DIARIA" -> 1;
            case "SEMANAL" -> 7;
            case "QUINCENAL" -> 15;
            case "MENSUAL" -> 0;
            default -> throw new IllegalArgumentException("Frecuencia de pago no válida.");
        };
        int numero = 1;
        LocalDate vencimiento;
        do {
            LocalDate programada = periodoDias == 0 ? inicio.plusMonths(numero) : inicio.plusDays((long) periodoDias * numero);
            vencimiento = programada.isAfter(limite) ? limite : programada;
            fechas.add(vencimiento);
            numero++;
        } while (!vencimiento.equals(limite));
        return fechas;
    }
}
