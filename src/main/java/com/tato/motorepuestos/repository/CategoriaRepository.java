package com.tato.motorepuestos.repository;

import com.tato.motorepuestos.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    List<Categoria> findByActivoTrue();

    boolean existsByNombre(String nombre);

    boolean existsByNombreAndIdNot(String nombre, Long id);

    @Query("SELECT COUNT(p) > 0 FROM Producto p WHERE p.categoria.id = :categoriaId")
    boolean existsProductoConCategoria(@Param("categoriaId") Long categoriaId);
}