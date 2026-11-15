package com.ud.cobranzas.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class ApiResponseDtos {
    private ApiResponseDtos() {
    }

    public record Cliente(Integer id, String nombre, String telefono) {
    }

    public record Cobrador(Integer id, String nombre, boolean activo) {
    }

    public record Credito(
            Integer id,
            Integer clienteId,
            String nombreCliente,
            Integer cobradorId,
            String nombreCobrador,
            BigDecimal monto,
            BigDecimal tasaInteres,
            LocalDate fechaInicio,
            LocalDate fechaLimite,
            String frecuenciaPago,
            String estado) {
    }

    public record Cuota(
            Integer id,
            Integer creditoId,
            Integer numeroCuota,
            BigDecimal montoCuota,
            BigDecimal montoPagado,
            LocalDate fechaVencimiento,
            String estado) {
    }

    public record Gestion(
            Integer id,
            Integer creditoId,
            Integer cuotaId,
            LocalDateTime fechaGestion,
            String tipoGestion,
            String resultado,
            String observaciones) {
    }

    public record Pago(Integer id, Integer cuotaId, BigDecimal monto, LocalDateTime fechaPago) {
    }
}
