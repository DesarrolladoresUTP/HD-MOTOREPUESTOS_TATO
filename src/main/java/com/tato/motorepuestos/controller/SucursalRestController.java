package com.tato.motorepuestos.controller;

import com.tato.motorepuestos.model.Sucursal;
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

}