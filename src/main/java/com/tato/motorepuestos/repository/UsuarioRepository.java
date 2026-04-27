package com.tato.motorepuestos.repository;

import com.tato.motorepuestos.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    List<Usuario> findAll();
    Usuario findByCorreoElectronico(String correoElectronico);
}