package com.tato.motorepuestos.controller;

import com.tato.motorepuestos.model.Sucursal;
import com.tato.motorepuestos.repository.InventarioSucursalRepository;
import com.tato.motorepuestos.service.SucursalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sucursales")
public class SucursalRestController {

    @Autowired
    private SucursalService sucursalService;
    @Autowired
    private InventarioSucursalRepository inventarioRepository;

    @GetMapping
    public List<Sucursal> listar() {
        return sucursalService.listarTodas();
    }

    @GetMapping("/activas")
    public List<Sucursal> listarActivas() {
        return sucursalService.listarActivas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sucursal> obtener(@PathVariable Long id) {
        Sucursal sucursal = sucursalService.obtenerPorId(id);
        if (sucursal != null) return ResponseEntity.ok(sucursal);
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Sucursal> crear(@RequestBody Sucursal sucursal) {
        return ResponseEntity.ok(sucursalService.guardarSucursal(sucursal));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Sucursal> actualizar(@PathVariable Long id, @RequestBody Sucursal detalles) {
        Sucursal sucursal = sucursalService.obtenerPorId(id);
        if (sucursal != null) {
            sucursal.setNombre(detalles.getNombre());
            sucursal.setDireccion(detalles.getDireccion());
            sucursal.setTelefono(detalles.getTelefono());
            return ResponseEntity.ok(sucursalService.guardarSucursal(sucursal));
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id, @RequestParam boolean activo) {
        sucursalService.cambiarEstado(id, activo);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            sucursalService.eliminarLogico(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/con-stock")
    public ResponseEntity<?> sucursalesConStock(@RequestBody List<Long> productoIds) {
        try {
            List<Sucursal> sucursales = sucursalService.listarActivas();
            if (productoIds == null || productoIds.isEmpty()) {
                return ResponseEntity.ok(sucursales);
            }
            List<Sucursal> disponibles = sucursales.stream()
                    .filter(s -> productoIds.stream().allMatch(pid ->
                            inventarioRepository.findByProductoIdAndSucursalId(pid, s.getId())
                                    .map(inv -> Boolean.TRUE.equals(inv.getActivo()) && inv.getStock() > 0)
                                    .orElse(false)
                    ))
                    .collect(java.util.stream.Collectors.toList());

            if (disponibles.isEmpty()) {
                return ResponseEntity.ok(sucursales);
            }
            return ResponseEntity.ok(disponibles);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error interno: " + e.getMessage());
        }
    }
}