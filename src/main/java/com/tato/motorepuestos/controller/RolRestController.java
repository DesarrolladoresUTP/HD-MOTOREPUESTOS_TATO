package com.tato.motorepuestos.controller;

import com.tato.motorepuestos.model.Rol;
import com.tato.motorepuestos.service.RolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RolRestController {

    @Autowired
    private RolService rolService;

    @GetMapping
    public List<Rol> listarTodos() {
        return rolService.listarTodos();
    }

    @GetMapping("/activos")
    public List<Rol> listarActivos() {
        return rolService.listarActivos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Rol> obtenerPorId(@PathVariable Long id) {
        Rol rol = rolService.obtenerPorId(id);
        if (rol == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(rol);
    }

    @PostMapping
    public ResponseEntity<?> guardarRol(@RequestBody Rol rol) {
        try {
            Rol guardado = rolService.guardarRol(rol);
            return ResponseEntity.ok(guardado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarRol(@PathVariable Long id, @RequestBody Rol rol) {
        try {
            Rol actualizado = rolService.actualizarRol(id, rol);
            return ResponseEntity.ok(actualizado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id, @RequestParam boolean activo) {
        try {
            rolService.cambiarEstado(id, activo);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarRol(@PathVariable Long id) {
        try {
            rolService.eliminarLogico(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}