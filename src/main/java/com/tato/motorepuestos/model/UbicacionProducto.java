package com.tato.motorepuestos.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "ubicaciones_producto")
@Data
public class UbicacionProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne
    @JoinColumn(name = "sucursal_id", nullable = false)
    private Sucursal sucursal;

    private Integer estante;
    private Integer fila;
    private Integer columna;
}