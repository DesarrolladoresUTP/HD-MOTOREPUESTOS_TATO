package com.tato.motorepuestos.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "roles")
@Data
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(nullable = false)
    private Boolean activo = true;

    // Módulo Gestiones (5)
    private Boolean permisoUsuarios = false;
    private Boolean permisoRoles = false;
    private Boolean permisoProductos = false;
    private Boolean permisoCategorias = false;
    private Boolean permisoSucursales = false;

    // Módulo Inventario (3)
    private Boolean permisoStocks = false;
    private Boolean permisoTraslados = false;
    private Boolean permisoHistorial = false;

    // Módulo Compras (2)
    private Boolean permisoComprasIngresar = false;
    private Boolean permisoComprasRegistro = false;

    // Módulo Ventas (2)
    private Boolean permisoVentasRealizar = false;
    private Boolean permisoVentasRegistro = false;

}