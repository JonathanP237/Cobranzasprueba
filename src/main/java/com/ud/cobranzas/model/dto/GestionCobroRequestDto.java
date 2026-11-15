package com.ud.cobranzas.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record GestionCobroRequestDto(
        @NotNull Integer cuotaId,
        @NotBlank @Pattern(regexp = "LLAMADA|MENSAJE|VISITA") String tipoGestion,
        @NotBlank @Pattern(regexp = "PAGO_CONFIRMADO|SIN_RESPUESTA|PROMESA_PAGO|ACUERDO_PAGO|REFINANCIACION|CONTACTO_EXITOSO|RECORDATORIO") String resultado,
        @Size(max = 4000) String observaciones) {
}
