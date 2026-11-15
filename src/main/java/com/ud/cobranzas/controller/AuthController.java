package com.ud.cobranzas.controller;

import com.ud.cobranzas.model.dto.AuthResponseDto;
import com.ud.cobranzas.model.dto.LoginRequestDto;
import com.ud.cobranzas.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody LoginRequestDto request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
