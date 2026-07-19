package com.tato.motorepuestos.controller;

import com.tato.motorepuestos.service.TrasladoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/traslados")
public class TrasladoRestController {

    @Autowired
    private TrasladoService trasladoService;

    @PostMapping
    public ResponseEntity<?> ejecutarTraslado(@RequestBody Map<String, Object> payload, HttpSession session) {
        Long sucursalOrigenId = (Long) session.getAttribute("sucursalId");
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        Long sucursalDestinoId = Long.parseLong(payload.get("sucursalDestinoId").toString());
        List<Map<String, Object>> items = (List<Map<String, Object>>) payload.get("items");

        try {
            trasladoService.procesarTraslado(sucursalOrigenId, sucursalDestinoId, items, usuarioId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // NUEVO: Método GET para cargar el historial de traslados en el frontend
    @GetMapping
    public ResponseEntity<?> listarTraslados() {
        try {
            // Devuelve la lista completa de traslados.
            // Asegúrate de que el método en tu TrasladoService se llame 'listarTodos()'
            return ResponseEntity.ok(trasladoService.listarTodos());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al cargar historial: " + e.getMessage());
        }
    }
}