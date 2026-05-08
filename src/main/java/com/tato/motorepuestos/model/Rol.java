package com.tato.motorepuestos.model;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
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

    private boolean permisoComprasIngresar;
    private boolean permisoComprasRegistro;
    private boolean permisoVentasRealizar;
    private boolean permisoVentasRegistro;

    public Rol() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public boolean isPermisoUsuarios() { return permisoUsuarios; }
    public void setPermisoUsuarios(boolean permisoUsuarios) { this.permisoUsuarios = permisoUsuarios; }

    public boolean isPermisoRoles() { return permisoRoles; }
    public void setPermisoRoles(boolean permisoRoles) { this.permisoRoles = permisoRoles; }

    public boolean isPermisoProductos() { return permisoProductos; }
    public void setPermisoProductos(boolean permisoProductos) { this.permisoProductos = permisoProductos; }

    public boolean isPermisoCategorias() { return permisoCategorias; }
    public void setPermisoCategorias(boolean permisoCategorias) { this.permisoCategorias = permisoCategorias; }

    public boolean isPermisoSucursales() { return permisoSucursales; }
    public void setPermisoSucursales(boolean permisoSucursales) { this.permisoSucursales = permisoSucursales; }

    public boolean isPermisoStocks() { return permisoStocks; }
    public void setPermisoStocks(boolean permisoStocks) { this.permisoStocks = permisoStocks; }

    public boolean isPermisoTraslados() { return permisoTraslados; }
    public void setPermisoTraslados(boolean permisoTraslados) { this.permisoTraslados = permisoTraslados; }

    public boolean isPermisoHistorial() { return permisoHistorial; }
    public void setPermisoHistorial(boolean permisoHistorial) { this.permisoHistorial = permisoHistorial; }

    public boolean isPermisoComprasIngresar() { return permisoComprasIngresar; }
    public void setPermisoComprasIngresar(boolean permisoComprasIngresar) { this.permisoComprasIngresar = permisoComprasIngresar; }

    public boolean isPermisoComprasRegistro() { return permisoComprasRegistro; }
    public void setPermisoComprasRegistro(boolean permisoComprasRegistro) { this.permisoComprasRegistro = permisoComprasRegistro; }

    public boolean isPermisoVentasRealizar() { return permisoVentasRealizar; }
    public void setPermisoVentasRealizar(boolean permisoVentasRealizar) { this.permisoVentasRealizar = permisoVentasRealizar; }

    public boolean isPermisoVentasRegistro() { return permisoVentasRegistro; }
    public void setPermisoVentasRegistro(boolean permisoVentasRegistro) { this.permisoVentasRegistro = permisoVentasRegistro; }
}