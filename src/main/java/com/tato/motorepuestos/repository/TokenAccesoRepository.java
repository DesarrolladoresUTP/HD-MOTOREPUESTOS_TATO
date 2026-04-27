package com.tato.motorepuestos.repository;

import com.tato.motorepuestos.model.TokenAcceso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TokenAccesoRepository extends JpaRepository<TokenAcceso, Long> {
    Optional<TokenAcceso> findByToken(String token);
}