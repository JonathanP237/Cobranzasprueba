package com.ud.cobranzas.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CuotaVencidaDto(
        Integer cuotaId,
        Integer creditoId,
        Integer numeroCuota,
        String nombreCliente,
        String telefonoCliente,
        LocalDate fechaVencimiento,
        Integer diasMora,
        BigDecimal montoCuota,
        BigDecimal totalPagado,
        BigDecimal saldoPendiente
) {
}