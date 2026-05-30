package com.tato.motorepuestos.repository;

import com.tato.motorepuestos.model.UsuarioCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioClienteRepository extends JpaRepository<UsuarioCliente, Long> {

    Optional<UsuarioCliente> findByEmail(String email);

    Optional<UsuarioCliente> findByNumeroDocumento(String numeroDocumento);

    Optional<UsuarioCliente> findByTokenRecuperacion(String tokenRecuperacion);
}