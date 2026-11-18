package com.ud.cobranzas.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    @Query(value = """
            SELECT
                r.credito_id AS "creditoId",
                r.nombre_cliente AS "nombreCliente",
                r.capital AS "capital",
                r.tasa_pct AS "tasaPct",
                r.estado AS "estado",
                r.exceso_sobre_promedio AS "excesoSobrePromedio"
            FROM fn_reporte_creditos_promedio(:cobradorId) r
            ORDER BY r.capital DESC
            """, nativeQuery = true)
    List<CreditoPromedioFila> obtenerCreditosSobrePromedio(
            @Param("cobradorId") Integer cobradorId);

    interface CreditoPromedioFila {
        Integer getCreditoId();

        String getNombreCliente();

        BigDecimal getCapital();

        BigDecimal getTasaPct();

        String getEstado();

        BigDecimal getExcesoSobrePromedio();
    }

    @Query(value = """
            SELECT
                r.cliente_id AS "clienteId",
                r.nombre_cliente AS "nombreCliente",
                r.telefono_cliente AS "telefonoCliente"
            FROM fn_reporte_clientes_morosos_exists(:cobradorId) r
            ORDER BY r.nombre_cliente
            """, nativeQuery = true)
    List<ClienteMorosoFila> obtenerClientesMorosos(
            @Param("cobradorId") Integer cobradorId);

    interface ClienteMorosoFila {

        Integer getClienteId();

        String getNombreCliente();

        String getTelefonoCliente();
    }

    @Query(value = """
            SELECT
                v.cuota_id AS "cuotaId",
                v.credito_id AS "creditoId",
                v.numero_cuota AS "numeroCuota",
                v.nombre_cliente AS "nombreCliente",
                v.telefono_cliente AS "telefonoCliente",
                v.fecha_vencimiento AS "fechaVencimiento",
                (CURRENT_DATE - v.fecha_vencimiento) AS "diasMora",
                v.monto_cuota AS "montoCuota",
                COALESCE(ab.total_pagado, 0) AS "totalPagado",
                GREATEST(
                    v.monto_cuota - COALESCE(ab.total_pagado, 0),
                    0
                ) AS "saldoPendiente"
            FROM vista_cuotas_vencidas v
            JOIN creditos cr ON cr.id = v.credito_id
            LEFT JOIN (
                SELECT
                    cuota_id,
                    SUM(monto) AS total_pagado
                FROM pagos
                GROUP BY cuota_id
            ) ab ON ab.cuota_id = v.cuota_id
            WHERE cr.cobrador_id = :cobradorId
              AND cr.estado = 'AL_DIA'
              AND v.monto_cuota > COALESCE(ab.total_pagado, 0)
            ORDER BY v.fecha_vencimiento ASC, v.cuota_id
            """, nativeQuery = true)
    List<CuotaVencidaFila> obtenerCuotasVencidas(
            @Param("cobradorId") Integer cobradorId);

    interface CuotaVencidaFila {

        Integer getCuotaId();

        Integer getCreditoId();

        Integer getNumeroCuota();

        String getNombreCliente();

        String getTelefonoCliente();

        LocalDate getFechaVencimiento();

        Integer getDiasMora();

        BigDecimal getMontoCuota();

        BigDecimal getTotalPagado();

        BigDecimal getSaldoPendiente();
    }

    @Query(value = """
            SELECT
                v.credito_id AS "creditoId",
                v.nombre_cliente AS "nombreCliente",
                v.frecuencia_pago AS "frecuenciaPago",
                v.capital_prestado AS "capitalPrestado",
                v.tasa_pct AS "tasaPct",
                v.interes_generado AS "interesGenerado",
                v.total_a_recaudar AS "totalARecaudar",
                v.estado_credito AS "estadoCredito",
                v.total_recaudado AS "totalRecaudado",
                v.saldo_pendiente AS "saldoPendiente",
                v.interes_cubierto_apto_refinanciar
                    AS "indicadorIntereses"
            FROM vista_resumen_creditos v
            JOIN creditos cr ON cr.id = v.credito_id
            WHERE cr.cobrador_id = :cobradorId
            ORDER BY v.saldo_pendiente DESC, v.credito_id
            """, nativeQuery = true)
    List<ResumenMaterializadoFila> obtenerResumenMaterializado(
            @Param("cobradorId") Integer cobradorId);

    interface ResumenMaterializadoFila {

        Integer getCreditoId();

        String getNombreCliente();

        String getFrecuenciaPago();

        BigDecimal getCapitalPrestado();

        BigDecimal getTasaPct();

        BigDecimal getInteresGenerado();

        BigDecimal getTotalARecaudar();

        String getEstadoCredito();

        BigDecimal getTotalRecaudado();

        BigDecimal getSaldoPendiente();

        Boolean getIndicadorIntereses();
    }
}
