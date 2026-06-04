package com.tato.motorepuestos.repository;

import com.tato.motorepuestos.model.PedidoWeb;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PedidoWebRepository extends JpaRepository<PedidoWeb, Long> {
    List<PedidoWeb> findAllByOrderByFechaPedidoDesc();
    List<PedidoWeb> findByUsuarioClienteIdOrderByFechaPedidoDesc(Long clienteWebId);
}