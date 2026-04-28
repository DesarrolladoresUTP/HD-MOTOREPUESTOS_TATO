package com.tato.motorepuestos.controller;

import com.tato.motorepuestos.model.Categoria;
import com.tato.motorepuestos.service.CategoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaRestController {

    @Autowired
    private CategoriaService categoriaService;

    @GetMapping
    public List<Categoria> listar() {
        return categoriaService.listarTodas();
    }

    @GetMapping("/activas")
    public List<Categoria> listarActivas() {
        return categoriaService.listarActivas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Categoria> obtener(@PathVariable Long id) {
        Categoria categoria = categoriaService.obtenerPorId(id);
        if (categoria != null) return ResponseEntity.ok(categoria);
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Categoria> crear(@RequestBody Categoria categoria) {
        return ResponseEntity.ok(categoriaService.guardarCategoria(categoria));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Categoria> actualizar(@PathVariable Long id, @RequestBody Categoria detalles) {
        Categoria categoria = categoriaService.obtenerPorId(id);
        if (categoria != null) {
            categoria.setNombre(detalles.getNombre());
            categoria.setDescripcion(detalles.getDescripcion());
            return ResponseEntity.ok(categoriaService.guardarCategoria(categoria));
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id, @RequestParam boolean activo) {
        categoriaService.cambiarEstado(id, activo);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            categoriaService.eliminarLogico(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}