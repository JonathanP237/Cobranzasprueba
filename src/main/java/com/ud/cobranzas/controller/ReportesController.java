package com.ud.cobranzas.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.ud.cobranzas.model.dto.ClienteMorosoDto;
import com.ud.cobranzas.model.dto.CreditoSobrePromedioDto;
import com.ud.cobranzas.model.dto.CuotaVencidaDto;
import com.ud.cobranzas.model.dto.RecaudoDto;
import com.ud.cobranzas.model.dto.ResumenCarteraDto;
import com.ud.cobranzas.model.entity.CobradorEntity;
import com.ud.cobranzas.service.ReportesService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReportesController {

        private final ReportesService reportesService;

        @GetMapping("/recaudos")
        public ResponseEntity<List<RecaudoDto>> historialRecaudos(
                        @AuthenticationPrincipal CobradorEntity cobradorAuth) {

                if (cobradorAuth == null) {
                        throw new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Debes iniciar sesión.");
                }

                return ResponseEntity.ok(
                                reportesService.obtenerRecaudos(
                                                cobradorAuth.getId()));
        }

        @GetMapping("/creditos-sobre-promedio")
        public ResponseEntity<List<CreditoSobrePromedioDto>> creditosSobrePromedio(
                        @AuthenticationPrincipal CobradorEntity cobradorAuth) {

                if (cobradorAuth == null) {
                        throw new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Debes iniciar sesión.");
                }

                return ResponseEntity.ok(
                                reportesService.obtenerCreditosSobrePromedio(
                                                cobradorAuth.getId()));
        }

        @GetMapping("/cartera")
        public ResponseEntity<ResumenCarteraDto> resumenCartera(
                        @AuthenticationPrincipal CobradorEntity cobradorAuth) {

                if (cobradorAuth == null) {
                        throw new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Debes iniciar sesión.");
                }

                return ResponseEntity.ok(
                                reportesService.obtenerResumenCartera(
                                                cobradorAuth.getId()));
        }

        @GetMapping("/clientes-morosos")
        public ResponseEntity<List<ClienteMorosoDto>> clientesMorosos(
                        @AuthenticationPrincipal CobradorEntity cobradorAuth) {

                if (cobradorAuth == null) {
                        throw new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Debes iniciar sesión.");
                }

                return ResponseEntity.ok(
                                reportesService.obtenerClientesMorosos(
                                                cobradorAuth.getId()));
        }

        @GetMapping("/cuotas-vencidas")
        public ResponseEntity<List<CuotaVencidaDto>> cuotasVencidas(
                        @AuthenticationPrincipal CobradorEntity cobradorAuth) {

                if (cobradorAuth == null) {
                        throw new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Debes iniciar sesión.");
                }

                return ResponseEntity.ok(
                                reportesService.obtenerCuotasVencidas(
                                                cobradorAuth.getId()));
        }
}
