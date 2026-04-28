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

    @GetMapping("/usuarios")
    public String mostrarUsuarios() {
        return "forward:/usuarios.html";
    }

    @GetMapping("/permisos")
    public String mostrarPermisos() {
        return "forward:/permisos.html";
    }

    @GetMapping("/productos")
    public String mostrarProductos() {
        return "forward:/productos.html";
    }

    @GetMapping("/categorias")
    public String mostrarCategorias() {
        return "forward:/categorias.html";
    }

    @GetMapping("/sucursales")
    public String mostrarSucursales() {
        return "forward:/sucursales.html";
    }

    @GetMapping("/stocks")
    public String mostrarStocks() {
        return "forward:/stocks.html";
    }

    @GetMapping("/traslados")
    public String mostrarTraslados() {
        return "forward:/traslados.html";
    }

}