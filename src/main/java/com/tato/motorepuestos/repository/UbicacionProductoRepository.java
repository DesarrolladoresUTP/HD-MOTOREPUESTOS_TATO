package com.tato.motorepuestos.repository;

import com.tato.motorepuestos.model.UbicacionProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UbicacionProductoRepository extends JpaRepository<UbicacionProducto, Long> {
    Optional<UbicacionProducto> findByProductoIdAndSucursalId(Long productoId, Long sucursalId);
}