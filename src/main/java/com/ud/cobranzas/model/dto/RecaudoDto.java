package com.ud.cobranzas.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RecaudoDto(
        Integer pagoId,
        Integer creditoId,
        Integer cuotaNumero,
        String nombreCobrador,
        String nombreCliente,
        BigDecimal montoAbono,
        LocalDateTime fechaPago
) {
}