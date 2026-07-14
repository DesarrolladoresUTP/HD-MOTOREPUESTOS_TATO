package com.tato.motorepuestos.repository;

import com.tato.motorepuestos.model.PedidoAlmacen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PedidoAlmacenRepository extends JpaRepository<PedidoAlmacen, Long> {
    Optional<PedidoAlmacen> findByVentaId(Long ventaId);
}