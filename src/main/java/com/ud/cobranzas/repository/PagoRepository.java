package com.ud.cobranzas.repository;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ud.cobranzas.model.entity.PagoEntity;

@Repository
public interface PagoRepository extends JpaRepository<PagoEntity, Integer> {
    @Query("SELECT COALESCE(SUM(p.monto), 0) FROM PagoEntity p WHERE p.cuota.id = :cuotaId")
    BigDecimal calcularTotalPagadoPorCuota(@Param("cuotaId") Integer cuotaId);
}