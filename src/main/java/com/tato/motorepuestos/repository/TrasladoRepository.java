package com.tato.motorepuestos.repository;

import com.tato.motorepuestos.model.Traslado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrasladoRepository extends JpaRepository<Traslado, Long> {

    // Spring Data JPA creará automáticamente este método por el nombre
    // Ordena el historial para mostrar siempre el más reciente primero
    List<Traslado> findAllByOrderByFechaRegistroDesc();
}