package com.tato.motorepuestos.controller;

import com.tato.motorepuestos.model.InventarioSucursal;
import com.tato.motorepuestos.service.ProductoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PutMapping("/{inventarioId}/ajuste")
    public ResponseEntity<?> ajustarStock(
            @PathVariable Long inventarioId,
            @RequestParam("stock") Integer stock,
            @RequestParam("stockMinimo") Integer stockMinimo,
            HttpSession session) {
        try {
            Long sucursalId = (Long) session.getAttribute("sucursalId");
            Long usuarioId = (Long) session.getAttribute("usuarioId");

            productoService.ajustarInventario(inventarioId, stock, stockMinimo, usuarioId, sucursalId);
            return ResponseEntity.ok("Stock actualizado correctamente en almacén.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}