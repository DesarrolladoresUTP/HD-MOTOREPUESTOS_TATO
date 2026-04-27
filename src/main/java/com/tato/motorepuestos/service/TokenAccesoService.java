package com.tato.motorepuestos.service;

import com.tato.motorepuestos.model.TokenAcceso;
import com.tato.motorepuestos.model.TokenAcceso.TipoToken;
import com.tato.motorepuestos.model.Usuario;
import com.tato.motorepuestos.repository.TokenAccesoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class TokenAccesoService {

    @Autowired
    private TokenAccesoRepository tokenRepo;

    public String generarToken(Usuario usuario, TipoToken tipo) {
        TokenAcceso t = new TokenAcceso();
        t.setUsuario(usuario);
        t.setToken(UUID.randomUUID().toString());
        t.setTipo(tipo);
        t.setUsado(false);
        t.setFechaExpiracion(LocalDateTime.now().plusHours(24));
        tokenRepo.save(t);
        return t.getToken();
    }

    public Optional<TokenAcceso> buscarVigente(String token, TipoToken tipo) {
        return tokenRepo.findByToken(token)
                .filter(t -> t.getTipo() == tipo && t.estaVigente());
    }

    public void marcarUsado(TokenAcceso token) {
        token.setUsado(true);
        tokenRepo.save(token);
    }
}