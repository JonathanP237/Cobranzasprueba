package com.ud.cobranzas.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ud.cobranzas.model.entity.CreditoEntity;
import com.ud.cobranzas.model.entity.CuotaEntity;
import com.ud.cobranzas.repository.CreditoRepository;
import com.ud.cobranzas.repository.CuotaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CobradorConsultaService {

    private final CreditoRepository creditoRepository;
    private final CuotaRepository cuotaRepository;

    // Obtener todos los créditos asignados a un cobrador específico
    public List<CreditoEntity> obtenerCreditosPorCobrador(Integer cobradorId) {
        return creditoRepository.findByCobradorId(cobradorId);
    }

    // Obtener las cuotas pendientes de un crédito asegurando que pertenezcan al cobrador
    public List<CuotaEntity> obtenerCuotasPendientesPorCredito(Integer cobradorId, Integer creditoId) {
        CreditoEntity credito = creditoRepository.findById(creditoId)
                .orElseThrow(() -> new RuntimeException("Crédito no encontrado"));

        // Validar que el crédito pertenezca al cobrador
        if (!credito.getCobrador().getId().equals(cobradorId)) {
            throw new SecurityException("Acceso denegado: Este crédito no pertenece al cobrador.");
        }

        return cuotaRepository.findByCreditoIdAndEstado(creditoId, "PENDIENTE");
    }
}