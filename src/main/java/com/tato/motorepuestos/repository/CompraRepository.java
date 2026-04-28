package com.tato.motorepuestos.repository;

import com.tato.motorepuestos.model.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {

    List<Compra> findBySucursalIdOrderByFechaEmisionDesc(Long sucursalId);
}