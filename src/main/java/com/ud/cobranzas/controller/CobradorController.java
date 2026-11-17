package com.ud.cobranzas.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.ud.cobranzas.model.dto.ApiResponseDtos;
import com.ud.cobranzas.model.dto.GestionCobroRequestDto;
import com.ud.cobranzas.model.dto.PagoRequestDto;
import com.ud.cobranzas.model.entity.CobradorEntity;
import com.ud.cobranzas.model.entity.CreditoEntity;
import com.ud.cobranzas.model.entity.CuotaEntity;
import com.ud.cobranzas.model.entity.GestionCobroEntity;
import com.ud.cobranzas.model.entity.PagoEntity;
import com.ud.cobranzas.repository.CuotaRepository;
import com.ud.cobranzas.repository.GestionCobroRepository;
import com.ud.cobranzas.service.CobradorConsultaService;
import com.ud.cobranzas.service.PagoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cobradores")
@RequiredArgsConstructor
public class CobradorController {

        private final CobradorConsultaService consultaService;
        private final PagoService pagoService;
        private final CuotaRepository cuotaRepository;
        private final GestionCobroRepository gestionCobroRepository;

        private void validarCobradorAutenticado(
                        Integer cobradorId,
                        CobradorEntity cobradorAuth) {

                // 1. Validar que exista un usuario autenticado
                if (cobradorAuth == null) {
                        throw new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Debes iniciar sesión para acceder a este recurso.");
                }

                // 2. Validar que el cobrador solo acceda a sus propios datos
                if (!cobradorAuth.getId().equals(cobradorId)) {
                        throw new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Recurso no encontrado.");
                }
        }

        // 1. Endpoint para que el cobrador consulte sus créditos asignados
        // Ejemplo: GET /api/cobradores/3/creditos
        @GetMapping("/{cobradorId}/creditos")
        public ResponseEntity<List<ApiResponseDtos.Credito>> listarCreditosDelCobrador(
                        @PathVariable Integer cobradorId,
                        @AuthenticationPrincipal CobradorEntity cobradorAuth) {
                validarCobradorAutenticado(cobradorId, cobradorAuth);
                List<ApiResponseDtos.Credito> creditos = consultaService.obtenerCreditosPorCobrador(cobradorId)
                                .stream()
                                .map(c -> new ApiResponseDtos.Credito(
                                                c.getId(), c.getCliente().getId(), c.getCliente().getNombreCliente(),
                                                c.getCobrador().getId(), c.getCobrador().getNombreCobrador(),
                                                c.getMonto(), c.getTasaInteres(), c.getFechaInicio(),
                                                c.getFechaLimite(),
                                                c.getFrecuenciaPago(), c.getEstado()))
                                .toList();
                return ResponseEntity.ok(creditos);
        }

        // 2. Endpoint para consultar las cuotas pendientes de un crédito específico de
        // ese cobrador
        // Ejemplo: GET /api/cobradores/3/creditos/12/cuotas-pendientes
        @GetMapping("/{cobradorId}/creditos/{creditoId}/cuotas-pendientes")
        public ResponseEntity<List<ApiResponseDtos.Cuota>> listarCuotasPendientes(
                        @PathVariable Integer cobradorId,
                        @PathVariable Integer creditoId,
                        @AuthenticationPrincipal CobradorEntity cobradorAuth) {
                validarCobradorAutenticado(cobradorId, cobradorAuth);
                consultaService.obtenerCuotasPendientesPorCredito(cobradorId, creditoId);
                return ResponseEntity.ok(cuotaRepository.findCuotasConPagos(creditoId, "PENDIENTE").stream()
                                .map(c -> new ApiResponseDtos.Cuota(
                                                c.getId(), c.getCreditoId(), c.getNumeroCuota(),
                                                c.getMontoCuota(), c.getMontoPagado(), c.getFechaVencimiento(),
                                                c.getEstado()))
                                .toList());
        }

        // 3. Endpoint para que el cobrador registre el pago de una cuota en campo
        // Ejemplo: POST /api/cobradores/3/pagos
        @PostMapping("/{cobradorId}/pagos")
        public ResponseEntity<ApiResponseDtos.Pago> registrarPagoEnCampo(
                        @PathVariable Integer cobradorId,
                        @Valid @RequestBody PagoRequestDto request,
                        @AuthenticationPrincipal CobradorEntity cobradorAuth) {
                validarCobradorAutenticado(cobradorId, cobradorAuth);
                PagoEntity nuevoPago = pagoService.registrarPagoPorCobrador(cobradorId, request);
                return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponseDtos.Pago(
                                nuevoPago.getId(), nuevoPago.getCuota().getId(), nuevoPago.getMonto(),
                                nuevoPago.getFechaPago()));
        }

        @PostMapping("/{cobradorId}/gestiones")
        public ResponseEntity<ApiResponseDtos.Gestion> registrarGestion(
                        @PathVariable Integer cobradorId,
                        @Valid @RequestBody GestionCobroRequestDto request,
                        @AuthenticationPrincipal CobradorEntity cobradorAuth) {
                validarCobradorAutenticado(cobradorId, cobradorAuth);
                CuotaEntity cuota = cuotaRepository.findById(request.cuotaId())
                                .orElseThrow(() -> new IllegalArgumentException("Cuota no encontrada."));
                CreditoEntity credito = cuota.getCredito();
                if (!credito.getCobrador().getId().equals(cobradorId)) {
                        throw new SecurityException("Acceso denegado: Este crédito no está asignado a este cobrador.");
                }

                GestionCobroEntity gestion = gestionCobroRepository.save(GestionCobroEntity.builder()
                                .credito(credito)
                                .cuota(cuota)
                                .fechaGestion(LocalDateTime.now())
                                .tipoGestion(request.tipoGestion())
                                .resultado(request.resultado())
                                .observaciones(request.observaciones())
                                .build());
                return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponseDtos.Gestion(
                                gestion.getId(), credito.getId(), cuota.getId(), gestion.getFechaGestion(),
                                gestion.getTipoGestion(), gestion.getResultado(), gestion.getObservaciones()));
        }
}