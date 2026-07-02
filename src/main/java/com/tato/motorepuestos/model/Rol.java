package com.tato.motorepuestos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    private Boolean activo = true;

    private boolean permisoUsuarios;
    private boolean permisoRoles;
    private boolean permisoProductos;
    private boolean permisoCategorias;
    private boolean permisoSucursales;
    private boolean permisoStocks;
    private boolean permisoTraslados;
    private boolean permisoHistorial;
    private boolean permisoClientes;
    private boolean permisoWeb;
    private boolean permisoComprasIngresar;
    private boolean permisoComprasRegistro;
    private boolean permisoVentasRealizar;
    private boolean permisoVentasRegistro;

    @Column(nullable = false)
    private boolean permisoCajas;
    @Column(nullable = false)
    private boolean permisoCajasadmin;
}