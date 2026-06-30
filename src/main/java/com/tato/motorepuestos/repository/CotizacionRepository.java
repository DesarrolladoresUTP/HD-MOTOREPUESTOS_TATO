package com.tato.motorepuestos.repository;

import com.tato.motorepuestos.model.Cotizacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CotizacionRepository extends JpaRepository<Cotizacion, Long> {
    Optional<Cotizacion> findByCodigo(String codigo);

    List<Cotizacion> findAllByOrderByFechaEmisionDesc();
}