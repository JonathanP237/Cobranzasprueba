package com.ud.cobranzas.model.dto;

import java.math.BigDecimal;

public record CreditoSobrePromedioDto(
        Integer creditoId,
        String nombreCliente,
        BigDecimal capital,
        BigDecimal tasaPct,
        String estado,
        BigDecimal excesoSobrePromedio
) {
}