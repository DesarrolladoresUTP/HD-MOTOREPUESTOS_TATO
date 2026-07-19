package com.tato.motorepuestos.controller;

import com.tato.motorepuestos.model.InventarioSucursal;
import com.tato.motorepuestos.service.ProductoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/almacen")
public class AlmacenRestController {

    @Autowired
    private ProductoService productoService;

    @GetMapping
    public List<InventarioSucursal> listarInventario(HttpSession session) {
        Long sucursalId = (Long) session.getAttribute("sucursalId");
        return productoService.listarPorSucursal(sucursalId);
    }

    @PutMapping("/{id}/ajuste")
    public ResponseEntity<?> ajustarStock(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload,
            HttpSession session) {
        try {
            Long usuarioId = (Long) session.getAttribute("usuarioId");
            Long sucursalId = (Long) session.getAttribute("sucursalId");

            if (!payload.containsKey("variantes")) {
                Integer stock = Integer.parseInt(payload.get("stock").toString());
                Integer stockMinimo = Integer.parseInt(payload.get("stockMinimo").toString());
                String motivo = payload.get("motivo").toString();

                productoService.ajustarInventario(id, stock, stockMinimo, motivo, usuarioId, sucursalId);
            }
            else {
                Integer stockMinimo = Integer.parseInt(payload.get("stockMinimo").toString());
                String motivoGeneral = payload.get("motivo").toString();
                List<Map<String, Object>> variantes = (List<Map<String, Object>>) payload.get("variantes");

                productoService.ajustarInventarioVariantes(id, variantes, stockMinimo, motivoGeneral, usuarioId, sucursalId);
            }

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/historial")
    public ResponseEntity<?> verHistorial(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerHistorialInventario(id));
    }
}