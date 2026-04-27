package com.tato.motorepuestos.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class VistasController {

    @GetMapping({"/", "/login"})
    public String mostrarLogin() {
        return "forward:/login.html";
    }

    @GetMapping("/inicio")
    public String mostrarInicio() {
        return "forward:/inicio.html";
    }

    @GetMapping("/venta")
    public String mostrarVenta() {
        return "forward:/venta.html";
    }

    @GetMapping("/usuarios")
    public String mostrarUsuarios() {
        return "forward:/usuarios.html";
    }

}