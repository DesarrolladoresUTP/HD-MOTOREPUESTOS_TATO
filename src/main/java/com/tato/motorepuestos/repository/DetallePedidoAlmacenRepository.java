package com.tato.motorepuestos.repository;

import com.tato.motorepuestos.model.DetallePedidoAlmacen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DetallePedidoAlmacenRepository extends JpaRepository<DetallePedidoAlmacen, Long> {
}