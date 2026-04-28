package com.tato.motorepuestos.controller;

import com.tato.motorepuestos.model.Cliente;
import com.tato.motorepuestos.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clientes")
public class ClienteRestController {

    @Autowired
    private ClienteRepository clienteRepository;

    @GetMapping("/buscar")
    public ResponseEntity<?> buscarPorDocumento(@RequestParam String documento) {
        return clienteRepository.findByNumeroDocumento(documento)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @PostMapping
    public ResponseEntity<?> guardarOActualizar(@RequestBody Cliente cliente) {
        try {
            return clienteRepository.findByNumeroDocumento(cliente.getNumeroDocumento())
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> {
                        Cliente guardado = clienteRepository.save(cliente);
                        return ResponseEntity.ok(guardado);
                    });
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}