package com.tato.motorepuestos.controller;

import com.tato.motorepuestos.model.Compra;
import com.tato.motorepuestos.service.CompraService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/compras")
public class CompraRestController {

    @Autowired
    private CompraService compraService;

    @GetMapping
    public List<Compra> listar(HttpSession session) {
        Long sucursalId = (Long) session.getAttribute("sucursalId");
        return compraService.listarPorSucursal(sucursalId);
    }

    @PostMapping
    public ResponseEntity<?> registrarCompra(@RequestBody Map<String, Object> payload,
                                             HttpSession session) {
        Long usuarioId  = (Long) session.getAttribute("usuarioId");
        Long sucursalId = (Long) session.getAttribute("sucursalId");
        try {
            compraService.registrarCompra(payload, usuarioId, sucursalId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}