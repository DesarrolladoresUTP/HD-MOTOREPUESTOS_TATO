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
    public List<Rol> listar() {
        return rolService.listarTodos();
    }

    @GetMapping("/activos")
    public List<Rol> listarActivos() {
        return rolService.listarActivos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Rol> obtener(@PathVariable Long id) {
        Rol rol = rolService.obtenerPorId(id);
        if (rol != null) {
            return ResponseEntity.ok(rol);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Rol> crear(@RequestBody Rol rol) {
        rol.setActivo(true);
        return ResponseEntity.ok(rolService.guardarRol(rol));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Rol> actualizar(@PathVariable Long id, @RequestBody Rol detalles) {
        Rol rolExistente = rolService.obtenerPorId(id);
        if (rolExistente != null) {
            rolExistente.setNombre(detalles.getNombre());
            rolExistente.setPermisoUsuarios(detalles.getPermisoUsuarios());
            rolExistente.setPermisoRoles(detalles.getPermisoRoles());
            rolExistente.setPermisoProductos(detalles.getPermisoProductos());
            rolExistente.setPermisoCategorias(detalles.getPermisoCategorias());
            rolExistente.setPermisoSucursales(detalles.getPermisoSucursales());
            rolExistente.setPermisoStocks(detalles.getPermisoStocks());
            rolExistente.setPermisoTraslados(detalles.getPermisoTraslados());
            rolExistente.setPermisoHistorial(detalles.getPermisoHistorial());
            rolExistente.setPermisoComprasIngresar(detalles.getPermisoComprasIngresar());
            rolExistente.setPermisoComprasRegistro(detalles.getPermisoComprasRegistro());
            rolExistente.setPermisoVentasRealizar(detalles.getPermisoVentasRealizar());
            rolExistente.setPermisoVentasRegistro(detalles.getPermisoVentasRegistro());

            return ResponseEntity.ok(rolService.guardarRol(rolExistente));
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id, @RequestParam boolean activo) {
        rolService.cambiarEstado(id, activo);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            rolService.eliminarLogico(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}