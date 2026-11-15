package com.ud.cobranzas.repository;

import java.util.Optional;
import java.util.List;
import java.math.BigDecimal;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ud.cobranzas.model.entity.CuotaEntity;

@Repository
public interface CuotaRepository extends JpaRepository<CuotaEntity, Integer> {
    List<CuotaEntity> findByCreditoId(Integer creditoId);
    List<CuotaEntity> findByCreditoIdAndEstado(Integer creditoId, String estado);

    @Query("""
            SELECT cu.id AS id, cu.credito.id AS creditoId, cu.numeroCuota AS numeroCuota,
                   cu.montoCuota AS montoCuota, COALESCE(SUM(p.monto), 0) AS montoPagado,
                   cu.fechaVencimiento AS fechaVencimiento, cu.estado AS estado
            FROM CuotaEntity cu
            LEFT JOIN PagoEntity p ON p.cuota.id = cu.id
            WHERE cu.credito.id = :creditoId
              AND (:estado IS NULL OR cu.estado = :estado)
            GROUP BY cu.id, cu.credito.id, cu.numeroCuota, cu.montoCuota,
                     cu.fechaVencimiento, cu.estado
            ORDER BY cu.numeroCuota
            """)
    List<CuotaConPagos> findCuotasConPagos(
            @Param("creditoId") Integer creditoId,
            @Param("estado") String estado);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CuotaEntity c WHERE c.id = :cuotaId")
    Optional<CuotaEntity> findByIdForUpdate(@Param("cuotaId") Integer cuotaId);

    interface CuotaConPagos {
        Integer getId();
        Integer getCreditoId();
        Integer getNumeroCuota();
        BigDecimal getMontoCuota();
        BigDecimal getMontoPagado();
        java.time.LocalDate getFechaVencimiento();
        String getEstado();
    }
}