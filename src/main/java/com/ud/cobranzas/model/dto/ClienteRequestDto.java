package com.ud.cobranzas.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClienteRequestDto(
        @NotBlank @Size(max = 150) String nombreCliente,
        @Size(max = 30) String telefonoCliente) {
}
