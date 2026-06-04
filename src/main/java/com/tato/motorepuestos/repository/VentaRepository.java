package com.tato.motorepuestos.repository;

import com.tato.motorepuestos.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    List<Venta> findBySucursalIdOrderByFechaDesc(Long sucursalId);


    @Query("SELECT MAX(v.numeroComprobante) FROM Venta v " +
            "WHERE v.tipoComprobante = :tipo AND v.serie = :serie")
    String findUltimoCorrelativo(@Param("tipo") String tipo, @Param("serie") String serie);
}