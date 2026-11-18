package com.ud.cobranzas.model.dto;

import java.math.BigDecimal;

public record ResumenMaterializadoDto(
        Integer creditoId,
        String nombreCliente,
        String frecuenciaPago,
        BigDecimal capitalPrestado,
        BigDecimal tasaPct,
        BigDecimal interesGenerado,
        BigDecimal totalARecaudar,
        String estadoCredito,
        BigDecimal totalRecaudado,
        BigDecimal saldoPendiente,
        Boolean indicadorIntereses
) {
}