package com.tato.motorepuestos.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "inventario_sucursal")
@Data
public class InventarioSucursal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne
    @JoinColumn(name = "sucursal_id", nullable = false)
    private Sucursal sucursal;

    @Column(nullable = false)
    private Integer stock = 0;

    @Column(nullable = false)
    private Integer stockMinimo = 5;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precioCompra = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precioVenta = BigDecimal.ZERO;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(nullable = false)
    private Integer unidadesVendidas = 0;


}