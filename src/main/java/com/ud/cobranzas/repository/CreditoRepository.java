package com.ud.cobranzas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ud.cobranzas.model.entity.CreditoEntity;

@Repository
public interface CreditoRepository extends JpaRepository<CreditoEntity, Integer> {
    List<CreditoEntity> findByClienteId(Integer clienteId);

    List<CreditoEntity> findByCobradorId(Integer cobradorId);

    @Query(value = "SELECT tasa_interes FROM creditos WHERE id = :creditoId", nativeQuery = true)
    java.math.BigDecimal obtenerTasaInteres(@Param("creditoId") Integer creditoId);
}