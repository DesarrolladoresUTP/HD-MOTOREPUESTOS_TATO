package com.tato.motorepuestos.repository;

import com.tato.motorepuestos.model.InventarioSucursal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventarioSucursalRepository extends JpaRepository<InventarioSucursal, Long> {

    List<InventarioSucursal> findBySucursalId(Long sucursalId);

    Optional<InventarioSucursal> findByProductoIdAndSucursalId(Long productoId, Long sucursalId);

    @Query("SELECT i FROM InventarioSucursal i WHERE i.sucursal.id = :sucursalId " +
            "AND i.stock <= i.stockMinimo AND i.activo = true ORDER BY i.stock ASC")
    List<InventarioSucursal> findBajoStockBySucursalId(@Param("sucursalId") Long sucursalId);

    boolean existsByProductoIdAndSucursalId(Long productoId, Long sucursalId);
}