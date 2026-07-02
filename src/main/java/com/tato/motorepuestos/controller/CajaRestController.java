package com.tato.motorepuestos.controller;

import com.tato.motorepuestos.model.Caja;
import com.tato.motorepuestos.service.CajaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/caja")
public class CajaRestController {

    @Autowired
    private CajaService cajaService;

    @GetMapping("/estado")
    public ResponseEntity<?> verEstado(HttpSession session) {
        Long usuarioId  = (Long) session.getAttribute("usuarioId");
        Long sucursalId = (Long) session.getAttribute("sucursalId");
        if (usuarioId == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(cajaService.obtenerResumenCaja(usuarioId, sucursalId));
    }

    @PostMapping("/abrir")
    public ResponseEntity<?> abrir(@RequestBody Map<String, Object> payload, HttpSession session) {
        Long usuarioId  = (Long) session.getAttribute("usuarioId");
        Long sucursalId = (Long) session.getAttribute("sucursalId");
        if (usuarioId == null) return ResponseEntity.status(401).build();
        try {
            BigDecimal monto = new BigDecimal(payload.get("montoInicial").toString());
            Caja caja = cajaService.abrirCaja(monto, usuarioId, sucursalId);
            return ResponseEntity.ok(Map.of(
                    "id", caja.getId(),
                    "montoInicial", caja.getMontoInicial(),
                    "fechaApertura", caja.getFechaApertura().toString(),
                    "estado", caja.getEstado()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/cerrar")
    public ResponseEntity<?> cerrar(@RequestBody Map<String, Object> payload, HttpSession session) {
        Long usuarioId  = (Long) session.getAttribute("usuarioId");
        Long sucursalId = (Long) session.getAttribute("sucursalId");
        if (usuarioId == null) return ResponseEntity.status(401).build();
        try {
            BigDecimal montoReal = new BigDecimal(payload.get("montoReal").toString());
            String obs = payload.getOrDefault("observaciones", "").toString();
            Caja caja = cajaService.cerrarCaja(montoReal, obs, usuarioId, sucursalId);
            return ResponseEntity.ok(Map.of(
                    "id", caja.getId(),
                    "montoInicial", caja.getMontoInicial(),
                    "montoEsperado", caja.getMontoEsperado(),
                    "montoReal", caja.getMontoReal(),
                    "diferencia", caja.getDiferencia(),
                    "estado", caja.getEstado()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}