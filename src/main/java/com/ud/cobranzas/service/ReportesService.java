package com.ud.cobranzas.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ud.cobranzas.model.dto.CreditoSobrePromedioDto;
import com.ud.cobranzas.model.dto.RecaudoDto;
import com.ud.cobranzas.model.dto.ResumenCarteraDto;
import com.ud.cobranzas.repository.ReportesRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportesService {

    private final ReportesRepository reportesRepository;

    @Transactional(readOnly = true)
    public List<RecaudoDto> obtenerRecaudos(Integer cobradorId) {

        return reportesRepository
                .obtenerRecaudos(cobradorId)
                .stream()
                .map(r -> new RecaudoDto(
                        r.getPagoId(),
                        r.getCreditoId(),
                        r.getCuotaNumero(),
                        r.getNombreCobrador(),
                        r.getNombreCliente(),
                        r.getMontoAbono(),
                        r.getFechaPago()))
                .toList();
    }

    @Transactional(readOnly = true)
    public ResumenCarteraDto obtenerResumenCartera(Integer cobradorId) {

        return reportesRepository.obtenerResumenCartera(cobradorId)
                .stream()
                .findFirst()
                .map(r -> new ResumenCarteraDto(
                        r.getCobradorId(),
                        r.getNombreCobrador(),
                        r.getTotalCreditos(),
                        r.getTotalCapital(),
                        r.getTotalInteres(),
                        r.getCarteraBruta()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cobrador no encontrado."));
    }

    @Transactional(readOnly = true)
    public List<CreditoSobrePromedioDto> obtenerCreditosSobrePromedio(
            Integer cobradorId) {

        return reportesRepository
                .obtenerCreditosSobrePromedio(cobradorId)
                .stream()
                .map(r -> new CreditoSobrePromedioDto(
                        r.getCreditoId(),
                        r.getNombreCliente(),
                        r.getCapital(),
                        r.getTasaPct(),
                        r.getEstado(),
                        r.getExcesoSobrePromedio()))
                .toList();
    }
}