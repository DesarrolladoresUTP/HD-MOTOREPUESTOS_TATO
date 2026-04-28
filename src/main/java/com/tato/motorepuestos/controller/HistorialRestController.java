package com.tato.motorepuestos.controller;

import com.tato.motorepuestos.model.Historial;
import com.tato.motorepuestos.service.HistorialService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/historial")
public class HistorialRestController {

    @Autowired
    private HistorialService historialService;

    @GetMapping
    public List<Historial> listar(HttpSession session) {
        Long sucursalId = (Long) session.getAttribute("sucursalId");
        return historialService.listarPorSucursal(sucursalId);
    }
}