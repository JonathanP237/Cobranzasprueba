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
import com.ud.cobranzas.model.dto.ClienteRequestDto;
import com.ud.cobranzas.model.dto.CreditoRequestDto;
import com.ud.cobranzas.model.entity.ClienteEntity;
import com.ud.cobranzas.model.entity.CobradorEntity;
import com.ud.cobranzas.model.entity.CreditoEntity;
import com.ud.cobranzas.repository.ClienteRepository;
import com.ud.cobranzas.repository.CobradorRepository;
import com.ud.cobranzas.repository.CreditoRepository;
import com.ud.cobranzas.repository.CuotaRepository;
import com.ud.cobranzas.repository.GestionCobroRepository;
import com.ud.cobranzas.service.CreditoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MainController {

        private final ClienteRepository clienteRepository;
        private final CobradorRepository cobradorRepository;
        private final CreditoRepository creditoRepository;
        private final CuotaRepository cuotaRepository;
        private final GestionCobroRepository gestionCobroRepository;
        private final CreditoService creditoService;

        @GetMapping("/clientes")
        public ResponseEntity<List<ApiResponseDtos.Cliente>> listarClientes(
                        @AuthenticationPrincipal CobradorEntity cobradorAuth) {

                var clientes = clienteRepository
                                .findByCobrador_IdOrderByNombreClienteAsc(
                                                cobradorAuth.getId());

                return ResponseEntity.ok(
                                clientes.stream()
                                                .map(c -> new ApiResponseDtos.Cliente(
                                                                c.getId(),
                                                                c.getNombreCliente(),
                                                                c.getTelefonoCliente()))
                                                .toList());
        }

        @PostMapping("/clientes")
        public ResponseEntity<ApiResponseDtos.Cliente> crearCliente(
                        @Valid @RequestBody ClienteRequestDto request,
                        @AuthenticationPrincipal CobradorEntity cobradorAuth) {

                ClienteEntity cliente = clienteRepository.save(
                                ClienteEntity.builder()
                                                .nombreCliente(request.nombreCliente().trim())
                                                .telefonoCliente(request.telefonoCliente())
                                                .fechaCreacion(LocalDateTime.now())
                                                .cobrador(cobradorAuth)
                                                .build());

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(new ApiResponseDtos.Cliente(
                                                cliente.getId(),
                                                cliente.getNombreCliente(),
                                                cliente.getTelefonoCliente()));
        }

        @GetMapping("/cobradores")
        public ResponseEntity<List<ApiResponseDtos.Cobrador>> listarCobradoresActivos() {
                return ResponseEntity.ok(cobradorRepository.findByActivoTrueOrderByNombreCobradorAsc().stream()
                                .map(c -> new ApiResponseDtos.Cobrador(c.getId(), c.getNombreCobrador(),
                                                Boolean.TRUE.equals(c.getActivo())))
                                .toList());
        }

        @GetMapping("/creditos")
        public ResponseEntity<List<ApiResponseDtos.Credito>> listarCreditos() {
                return ResponseEntity.ok(creditoRepository.findAll().stream().map(this::aDto).toList());
        }

        @PostMapping("/creditos")
        public ResponseEntity<ApiResponseDtos.Credito> crearCredito(
                        @Valid @RequestBody CreditoRequestDto request,
                        @AuthenticationPrincipal CobradorEntity cobradorAuth) {

                CreditoEntity credito = creditoService.crear(
                                request,
                                cobradorAuth.getId());

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(aDto(credito));
        }

        @GetMapping("/cuotas/credito/{creditoId}")
        public ResponseEntity<List<ApiResponseDtos.Cuota>> listarCuotasPorCredito(@PathVariable Integer creditoId) {
                return ResponseEntity.ok(cuotaRepository.findCuotasConPagos(creditoId, null).stream()
                                .map(c -> new ApiResponseDtos.Cuota(
                                                c.getId(), c.getCreditoId(), c.getNumeroCuota(),
                                                c.getMontoCuota(), c.getMontoPagado(), c.getFechaVencimiento(),
                                                c.getEstado()))
                                .toList());
        }

        @GetMapping("/creditos/{creditoId}/gestiones")
        public ResponseEntity<List<ApiResponseDtos.Gestion>> listarGestiones(@PathVariable Integer creditoId) {
                return ResponseEntity
                                .ok(gestionCobroRepository.findByCreditoIdOrderByFechaGestionDesc(creditoId).stream()
                                                .map(g -> new ApiResponseDtos.Gestion(
                                                                g.getId(), g.getCredito().getId(), g.getCuota().getId(),
                                                                g.getFechaGestion(), g.getTipoGestion(),
                                                                g.getResultado(), g.getObservaciones()))
                                                .toList());
        }

        private ApiResponseDtos.Credito aDto(CreditoEntity credito) {
                return new ApiResponseDtos.Credito(
                                credito.getId(),
                                credito.getCliente().getId(),
                                credito.getCliente().getNombreCliente(),
                                credito.getCobrador().getId(),
                                credito.getCobrador().getNombreCobrador(),
                                credito.getMonto(),
                                credito.getTasaInteres(),
                                credito.getFechaInicio(),
                                credito.getFechaLimite(),
                                credito.getFrecuenciaPago(),
                                credito.getEstado());
        }

        private void validarCreditoPropio(
                        Integer creditoId,
                        CobradorEntity cobradorAuth) {

                if (cobradorAuth == null) {
                        throw new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED);
                }

                creditoRepository.findById(creditoId)
                                .filter(c -> c.getCobrador().getId()
                                                .equals(cobradorAuth.getId()))
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Crédito no encontrado."));
        }
}
