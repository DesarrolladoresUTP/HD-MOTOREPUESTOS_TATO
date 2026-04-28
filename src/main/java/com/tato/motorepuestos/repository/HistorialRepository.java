package com.tato.motorepuestos.repository;

import com.tato.motorepuestos.model.Historial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistorialRepository extends JpaRepository<Historial, Long> {

    List<Historial> findAllByOrderByFechaDesc();

    List<Historial> findBySucursalIdOrderByFechaDesc(Long sucursalId);
}