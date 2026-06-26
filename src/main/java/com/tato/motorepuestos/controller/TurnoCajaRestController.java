package com.tato.motorepuestos.controller;

import com.tato.motorepuestos.model.TurnoCaja;
import com.tato.motorepuestos.service.TurnoCajaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/caja")
public class TurnoCajaRestController {

    @Autowired
    private TurnoCajaService turnoCajaService;

    @GetMapping("/estado")
    public ResponseEntity<?> verEstadoCaja() {
        TurnoCaja activa = turnoCajaService.obtenerCajaActiva(1L);
        return ResponseEntity.ok(activa != null ? activa : Map.of("estado", "CERRADA"));
    }

    @PostMapping("/abrir")
    public ResponseEntity<?> abrirCaja(@RequestBody Map<String, BigDecimal> payload) {
        try {
            TurnoCaja turno = turnoCajaService.abrirCaja(payload.get("montoInicial"), 1L, 1L);
            return ResponseEntity.ok(turno);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/cerrar")
    public ResponseEntity<?> cerrarCaja(@RequestBody Map<String, Object> payload) {
        try {
            BigDecimal montoReal = new BigDecimal(payload.get("montoReal").toString());
            String obs = payload.getOrDefault("observaciones", "").toString();
            TurnoCaja turno = turnoCajaService.cerrarCaja(montoReal, obs, 1L);
            return ResponseEntity.ok(turno);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/historial")
    public ResponseEntity<?> historialCajas() {
        return ResponseEntity.ok(turnoCajaService.listarHistorial(1L));
    }
}