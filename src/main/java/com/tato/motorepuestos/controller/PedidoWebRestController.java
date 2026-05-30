package com.tato.motorepuestos.controller;

import com.tato.motorepuestos.dto.PedidoWebDTO;
import com.tato.motorepuestos.model.PedidoWeb;
import com.tato.motorepuestos.service.PedidoWebService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos-web")
public class PedidoWebRestController {

    @Autowired
    private PedidoWebService pedidoService;

    @PostMapping("/procesar")
    public ResponseEntity<?> procesarPedido(@RequestBody PedidoWebDTO pedidoDTO, HttpSession session) {
        try {
            Long clienteWebId = (Long) session.getAttribute("clienteWebId");
            PedidoWeb nuevoPedido = pedidoService.procesarPedido(pedidoDTO, clienteWebId);
            return ResponseEntity.ok(nuevoPedido);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al procesar pedido: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<PedidoWeb>> listarPedidos() {
        return ResponseEntity.ok(pedidoService.listarTodos());
    }

    @GetMapping("/mis-pedidos")
    public ResponseEntity<?> obtenerMisPedidos(HttpSession session) {
        Long clienteWebId = (Long) session.getAttribute("clienteWebId");
        if (clienteWebId == null) {
            return ResponseEntity.status(401).body("Debes iniciar sesión para ver esta información.");
        }
        return ResponseEntity.ok(pedidoService.listarMisPedidos(clienteWebId));
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id, @RequestParam String estado) {
        try {
            pedidoService.actualizarEstado(id, estado);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}