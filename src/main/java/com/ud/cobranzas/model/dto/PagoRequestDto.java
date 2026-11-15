package com.ud.cobranzas.model.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PagoRequestDto {
    @NotNull
    private Integer cuotaId;         // ID de la cuota que se va a pagar

    @NotNull
    @Positive
    @Digits(integer = 10, fraction = 2)
    private BigDecimal monto;        // Monto abonado por el cliente

    private String tipoGestion;      // Opcional: Ej. 'VISITA', 'LLAMADA'
    private String observaciones;    // Opcional: Notas registradas por el cobrador
}