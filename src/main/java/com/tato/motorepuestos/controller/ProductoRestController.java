package com.tato.motorepuestos.controller;

import com.tato.motorepuestos.model.InventarioSucursal;
import com.tato.motorepuestos.model.Producto;
import com.tato.motorepuestos.model.Sucursal;
import com.tato.motorepuestos.service.ProductoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoRestController {

    @Autowired
    private ProductoService productoService;

    @GetMapping
    public List<InventarioSucursal> listar(HttpSession session) {
        Long sucursalId = (Long) session.getAttribute("sucursalId");
        return productoService.listarPorSucursal(sucursalId);
    }

    @GetMapping("/bajo-stock")
    public List<InventarioSucursal> listarBajoStock(HttpSession session) {
        Long sucursalId = (Long) session.getAttribute("sucursalId");
        return productoService.listarBajoStock(sucursalId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtener(@PathVariable Long id) {
        Producto producto = productoService.obtenerPorId(id);
        if (producto != null) return ResponseEntity.ok(producto);
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<?> crear(
            @RequestParam(value = "codigoInterno", required = false) String codigoInterno,
            @RequestParam("nombre") String nombre,
            @RequestParam(value = "descripcion", required = false) String descripcion,
            @RequestParam("categoriaId") Long categoriaId,
            @RequestParam("marca") String marca,
            @RequestParam(value = "precioCompra", required = false) BigDecimal precioCompra,
            @RequestParam("precioVenta") BigDecimal precioVenta,
            @RequestParam(value = "stock", required = false) Integer stock,
            @RequestParam("stockMinimo") Integer stockMinimo,
            @RequestParam(value = "imagen", required = false) MultipartFile imagen,
            HttpSession session) {
        try {
            Long sucursalId = (Long) session.getAttribute("sucursalId");
            Long usuarioId = (Long) session.getAttribute("usuarioId");
            InventarioSucursal resultado = productoService.guardarProducto(
                    codigoInterno, nombre, descripcion, marca, categoriaId,
                    precioCompra, precioVenta, stock, stockMinimo,
                    imagen, usuarioId, sucursalId);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{inventarioId}")
    public ResponseEntity<?> actualizar(
            @PathVariable Long inventarioId,
            @RequestParam(value = "codigoInterno", required = false) String codigoInterno,
            @RequestParam("nombre") String nombre,
            @RequestParam(value = "descripcion", required = false) String descripcion,
            @RequestParam("categoriaId") Long categoriaId,
            @RequestParam("marca") String marca,
            @RequestParam(value = "precioCompra", required = false) BigDecimal precioCompra,
            @RequestParam("precioVenta") BigDecimal precioVenta,
            @RequestParam(value = "stock", required = false) Integer stock,
            @RequestParam("stockMinimo") Integer stockMinimo,
            @RequestParam(value = "imagen", required = false) MultipartFile imagen,
            HttpSession session) {
        try {
            Long sucursalId = (Long) session.getAttribute("sucursalId");
            Long usuarioId = (Long) session.getAttribute("usuarioId");
            InventarioSucursal resultado = productoService.actualizarProducto(
                    inventarioId, codigoInterno, nombre, descripcion, marca, categoriaId,
                    precioCompra, precioVenta, stock, stockMinimo,
                    imagen, usuarioId, sucursalId);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{inventarioId}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long inventarioId,
                                           @RequestParam boolean activo, HttpSession session) {
        try {
            Long sucursalId = (Long) session.getAttribute("sucursalId");
            Long usuarioId = (Long) session.getAttribute("usuarioId");
            productoService.cambiarEstado(inventarioId, activo, usuarioId, sucursalId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{inventarioId}")
    public ResponseEntity<?> eliminar(@PathVariable Long inventarioId, HttpSession session) {
        try {
            Long sucursalId = (Long) session.getAttribute("sucursalId");
            Long usuarioId = (Long) session.getAttribute("usuarioId");
            productoService.eliminarLogico(inventarioId, usuarioId, sucursalId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}