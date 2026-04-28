package com.tato.motorepuestos.repository;

import com.tato.motorepuestos.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {
    List<Rol> findAll();
    List<Rol> findByActivoTrue();
}