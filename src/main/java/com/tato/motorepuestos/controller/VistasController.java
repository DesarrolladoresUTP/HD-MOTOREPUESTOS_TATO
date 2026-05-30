package com.tato.motorepuestos.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class VistasController {

    @GetMapping({"/", "/tienda"})
    public String mostrarTienda() {
        return "forward:/tienda.html";
    }

    @GetMapping("/login")
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

    @GetMapping("/historial")
    public String mostrarHistorial() {
        return "forward:/historial.html";
    }

    @GetMapping("/compras")
    public String mostrarCompras() {
        return "forward:/compras.html";
    }

    @GetMapping("/registro_compras")
    public String mostrarRegistroCompras() {
        return "forward:/registro_compras.html";
    }

    @GetMapping("/registro_ventas")
    public String mostrarRegistroVentas() {
        return "forward:/registro_ventas.html";
    }

    @GetMapping("/clientes")
    public String mostrarClientes() {
        return "forward:/clientes.html";
    }

    @GetMapping("/gestion-web")
    public String mostrarGestionWeb() {
        return "forward:/gestion-web.html";
    }

    @GetMapping("/login-cliente")
    public String mostrarLoginCliente() {
        return "forward:/login-cliente.html";
    }

    @GetMapping("/clientes-web")
    public String mostrarClientesWeb() {
        return "forward:/clientes-web.html";
    }

    @GetMapping("/almacen")
    public String mostrarAlmacen() {
        return "forward:/almacen.html";
    }

    @GetMapping("/checkout")
    public String mostrarCheckout() {
        return "forward:/checkout.html";
    }

    @GetMapping("/pedidos")
    public String mostrarPedidos() {return "forward:/pedidos.html";}

    @GetMapping("/mis-pedidos")
    public String mostrarMisPedidos() {return "forward:/mis-pedidos.html";}


}