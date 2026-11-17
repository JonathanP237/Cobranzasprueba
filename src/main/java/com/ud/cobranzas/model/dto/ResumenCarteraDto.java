package com.ud.cobranzas.model.dto;

import java.math.BigDecimal;

public record ResumenCarteraDto(
        Integer cobradorId,
        String nombreCobrador,
        Long totalCreditos,
        BigDecimal totalCapital,
        BigDecimal totalInteres,
        BigDecimal carteraBruta
) {
}