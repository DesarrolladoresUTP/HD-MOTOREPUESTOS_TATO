package com.tato.motorepuestos.repository;

import com.tato.motorepuestos.model.TurnoCaja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TurnoCajaRepository extends JpaRepository<TurnoCaja, Long> {
    Optional<TurnoCaja> findTopBySucursalIdAndEstadoOrderByIdDesc(Long sucursalId, String estado);

    List<TurnoCaja> findBySucursalIdOrderByFechaAperturaDesc(Long sucursalId);
}