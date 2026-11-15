package com.ud.cobranzas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ud.cobranzas.model.entity.CobradorEntity;
import java.util.List;
import java.util.Optional;

@Repository
public interface CobradorRepository extends JpaRepository<CobradorEntity, Integer> {
    List<CobradorEntity> findByActivoTrueOrderByNombreCobradorAsc();
    Optional<CobradorEntity> findByUsername(String username);
}