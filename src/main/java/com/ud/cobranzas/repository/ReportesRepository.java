package com.ud.cobranzas.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ud.cobranzas.model.entity.PagoEntity;

@Repository
public interface ReportesRepository
        extends JpaRepository<PagoEntity, Integer> {

    @Query(value = """
            SELECT
                r.pago_id AS "pagoId",
                r.credito_id AS "creditoId",
                r.cuota_numero AS "cuotaNumero",
                r.nombre_cobrador AS "nombreCobrador",
                r.nombre_cliente AS "nombreCliente",
                r.monto_abono AS "montoAbono",
                r.fecha_pago AS "fechaPago"
            FROM fn_reporte_pagos_inner_join(:cobradorId) r
            ORDER BY r.fecha_pago DESC, r.pago_id DESC
            """, nativeQuery = true)
    List<RecaudoFila> obtenerRecaudos(
            @Param("cobradorId") Integer cobradorId);

    @Query(value = """
            SELECT
                r.cobrador_id AS "cobradorId",
                r.nombre_cobrador AS "nombreCobrador",
                r.total_creditos AS "totalCreditos",
                r.total_capital AS "totalCapital",
                r.total_interes AS "totalInteres",
                r.cartera_bruta AS "carteraBruta"
            FROM fn_reporte_cartera_left_join(:cobradorId) r
            """, nativeQuery = true)
    List<CarteraFila> obtenerResumenCartera(
            @Param("cobradorId") Integer cobradorId);

    interface CarteraFila {
        Integer getCobradorId();

        String getNombreCobrador();

        Long getTotalCreditos();

        BigDecimal getTotalCapital();

        BigDecimal getTotalInteres();

        BigDecimal getCarteraBruta();
    }

    interface RecaudoFila {
        Integer getPagoId();

        Integer getCreditoId();

        Integer getCuotaNumero();

        String getNombreCobrador();

        String getNombreCliente();

        BigDecimal getMontoAbono();

        LocalDateTime getFechaPago();
    }
}