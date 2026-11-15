package com.ud.cobranzas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

import com.ud.cobranzas.model.entity.GestionCobroEntity;

@Repository
public interface GestionCobroRepository extends JpaRepository<GestionCobroEntity, Integer> {
    List<GestionCobroEntity> findByCreditoIdOrderByFechaGestionDesc(Integer creditoId);
}