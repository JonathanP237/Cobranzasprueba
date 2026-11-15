package com.ud.cobranzas.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuthResponseDto {
    private String token;
    private Integer cobradorId;
    private String username;
    private String nombreCobrador;
}
