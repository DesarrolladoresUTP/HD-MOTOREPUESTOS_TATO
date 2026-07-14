package com.tato.motorepuestos.controller;

import com.tato.motorepuestos.model.ConfigAlmacen;
import com.tato.motorepuestos.model.UbicacionProducto;
import com.tato.motorepuestos.service.AlmacenComplementarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/almacen-config")
public class AlmacenConfigRestController {

    @Autowired
    private AlmacenComplementarioService almacenComplementarioService;

    @GetMapping("/{sucursalId}")
    public ResponseEntity<?> getConfig(@PathVariable Long sucursalId) {
        try {
            ConfigAlmacen config = almacenComplementarioService.obtenerOCrearConfig(sucursalId);
            return ResponseEntity.ok(Map.of(
                    "sucursalId", sucursalId,
                    "totalEstantes", config.getTotalEstantes(),
                    "totalFilas", config.getTotalFilas(),
                    "totalColumnas", config.getTotalColumnas(),
                    "requiereConfirmacionAlmacenero", config.getRequiereConfirmacionAlmacenero()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping
    public ResponseEntity<?> guardarConfig(@RequestBody Map<String, Object> payload) {
        try {
            Long sucursalId = Long.valueOf(payload.get("sucursalId").toString());
            Integer totalEstantes = Integer.valueOf(payload.get("totalEstantes").toString());
            Integer totalFilas = Integer.valueOf(payload.get("totalFilas").toString());
            Integer totalColumnas = Integer.valueOf(payload.get("totalColumnas").toString());
            Boolean requiereConfirmacion = Boolean.valueOf(payload.get("requiereConfirmacionAlmacenero").toString());

            almacenComplementarioService.guardarConfig(sucursalId, totalEstantes, totalFilas, totalColumnas, requiereConfirmacion);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/productos-sin-ubicacion")
    public ResponseEntity<?> getProductosSinUbicacion(@RequestParam Long sucursalId) {
        try {
            List<Map<String, Object>> productos = almacenComplementarioService.getProductosSinUbicacion(sucursalId);
            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("No se pudo conectar con el backend complementario: " + e.getMessage());
        }
    }

    @PostMapping("/ubicaciones")
    public ResponseEntity<?> asignarUbicacion(@RequestBody Map<String, Object> payload) {
        try {
            Long productoId = Long.valueOf(payload.get("productoId").toString());
            Long sucursalId = Long.valueOf(payload.get("sucursalId").toString());
            Integer estante = Integer.valueOf(payload.get("estante").toString());
            Integer fila = Integer.valueOf(payload.get("fila").toString());
            Integer columna = Integer.valueOf(payload.get("columna").toString());

            UbicacionProducto ubicacion = almacenComplementarioService.asignarUbicacion(
                    productoId, sucursalId, estante, fila, columna);
            return ResponseEntity.ok(Map.of(
                    "productoId", productoId,
                    "abreviatura", "E" + ubicacion.getEstante() + " - F" + ubicacion.getFila() + " - C" + ubicacion.getColumna()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/productos-sin-perfil-ia")
    public ResponseEntity<?> getProductosSinPerfilIa() {
        try {
            return ResponseEntity.ok(almacenComplementarioService.getProductosSinPerfilIa());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("No se pudo conectar con el backend complementario: " + e.getMessage());
        }
    }

    @PostMapping("/productos/{id}/generar-perfil-ia")
    public ResponseEntity<?> generarPerfilIa(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(almacenComplementarioService.generarPerfilIa(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}