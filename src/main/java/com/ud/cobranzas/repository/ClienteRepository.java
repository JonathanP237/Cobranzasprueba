
package com.ud.cobranzas.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ud.cobranzas.model.entity.ClienteEntity;

@Repository
public interface ClienteRepository
        extends JpaRepository<ClienteEntity, Integer> {

    List<ClienteEntity>
        findByCobrador_IdOrderByNombreClienteAsc(
            Integer cobradorId
        );

    Optional<ClienteEntity>
        findByIdAndCobrador_Id(
            Integer clienteId,
            Integer cobradorId
        );
}
