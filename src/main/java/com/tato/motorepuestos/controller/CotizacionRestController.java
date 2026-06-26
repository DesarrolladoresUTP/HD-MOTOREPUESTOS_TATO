package com.tato.motorepuestos.controller;

import com.tato.motorepuestos.dto.CotizacionDTO;
import com.tato.motorepuestos.model.Cotizacion;
import com.tato.motorepuestos.service.CotizacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cotizaciones")
public class CotizacionRestController {

    @Autowired
    private CotizacionService cotizacionService;

    @PostMapping("/generar")
    public ResponseEntity<?> generarCotizacion(@RequestBody CotizacionDTO dto) {
        try {
            Cotizacion nueva = cotizacionService.generarCotizacion(dto);
            return ResponseEntity.ok(nueva);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/buscar/{codigo}")
    public ResponseEntity<?> buscarCotizacion(@PathVariable String codigo) {
        try {
            Cotizacion cotizacion = cotizacionService.buscarYValidarCotizacion(codigo);
            return ResponseEntity.ok(cotizacion);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Cotizacion>> listarCotizaciones() {
        return ResponseEntity.ok(cotizacionService.listarTodas());
    }

    @PostMapping("/{codigo}/enviar")
    public ResponseEntity<?> enviarPorCorreo(@PathVariable String codigo, @RequestParam String correo) {
        try {
            return ResponseEntity.ok("Cotización " + codigo + " enviada a " + correo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al enviar: " + e.getMessage());
        }
    }
}