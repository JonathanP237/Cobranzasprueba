package com.ud.cobranzas.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ud.cobranzas.model.dto.AuthResponseDto;
import com.ud.cobranzas.model.dto.LoginRequestDto;
import com.ud.cobranzas.model.entity.CobradorEntity;
import com.ud.cobranzas.repository.CobradorRepository;
import com.ud.cobranzas.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final CobradorRepository cobradorRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthResponseDto login(LoginRequestDto request) {
        if (request.getUsername() == null || request.getPassword() == null) {
            throw new IllegalArgumentException("Usuario y contraseña requeridos.");
        }

        CobradorEntity cobrador = cobradorRepository.findByUsername(request.getUsername().trim().toLowerCase())
                .orElseThrow(() -> new SecurityException("Credenciales inválidas: Cobrador no encontrado."));

        if (!cobrador.getActivo()) {
            throw new SecurityException("Acceso denegado: Cobrador inactivo.");
        }

        if (!passwordEncoder.matches(
                request.getPassword(),
                cobrador.getPassword())) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Credenciales inválidas.");
        }

        String token = jwtService.generateToken(cobrador.getId(), cobrador.getUsername(), cobrador.getNombreCobrador());

        return AuthResponseDto.builder()
                .token(token)
                .cobradorId(cobrador.getId())
                .username(cobrador.getUsername())
                .nombreCobrador(cobrador.getNombreCobrador())
                .build();
    }
}
