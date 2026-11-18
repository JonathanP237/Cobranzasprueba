package com.ud.cobranzas.model.dto;

public record ClienteMorosoDto(
        Integer clienteId,
        String nombreCliente,
        String telefonoCliente
) {
}