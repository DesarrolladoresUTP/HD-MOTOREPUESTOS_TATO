package com.tato.motorepuestos.repository;

import com.tato.motorepuestos.model.ConfigAlmacen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfigAlmacenRepository extends JpaRepository<ConfigAlmacen, Long> {
    Optional<ConfigAlmacen> findBySucursalId(Long sucursalId);
}