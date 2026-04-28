package com.tato.motorepuestos.repository;

import com.tato.motorepuestos.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Optional<Producto> findByCodigoInterno(String codigoInterno);

    Optional<Producto> findByNombre(String nombre);

    boolean existsByCodigoInterno(String codigoInterno);

    boolean existsByNombreAndIdNot(String nombre, Long id);
}