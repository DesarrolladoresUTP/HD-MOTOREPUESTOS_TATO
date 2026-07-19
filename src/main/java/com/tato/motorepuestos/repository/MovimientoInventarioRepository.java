package com.tato.motorepuestos.repository;

import com.tato.motorepuestos.model.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {
    // Busca los movimientos de un producto y los ordena del más nuevo al más viejo
    List<MovimientoInventario> findByInventarioIdOrderByFechaRegistroDesc(Long inventarioId);
}