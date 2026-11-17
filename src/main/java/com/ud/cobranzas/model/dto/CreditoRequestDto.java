
package com.ud.cobranzas.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record CreditoRequestDto(

    @NotNull
    Integer clienteId,

    @NotNull
    @Positive
    @Digits(integer = 10, fraction = 2)
    BigDecimal monto,

    @NotNull
    LocalDate fechaInicio,

    @NotNull
    LocalDate fechaLimite,

    @NotNull
    @Pattern(regexp = "DIARIA|SEMANAL|QUINCENAL|MENSUAL")
    String frecuenciaPago

) {}
