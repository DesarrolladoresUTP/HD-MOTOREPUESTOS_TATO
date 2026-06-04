package com.tato.motorepuestos.controller;

import com.tato.motorepuestos.model.UsuarioCliente;
import com.tato.motorepuestos.repository.UsuarioClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes-web-admin")
public class UsuarioClienteAdminRestController {

    @Autowired
    private UsuarioClienteRepository repository;

    @GetMapping
    public List<UsuarioCliente> listarTodos() {
        return repository.findAll();
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id, @RequestParam boolean activo) {
        try {
            UsuarioCliente cliente = repository.findById(id)
                    .orElseThrow(() -> new Exception("Cliente web no encontrado."));

            cliente.setActivo(activo);
            repository.save(cliente);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}