package com.tato.motorepuestos.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "pedidos_almacen")
@Data
public class PedidoAlmacen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "venta_id")
    private Venta venta;

    @ManyToOne
    @JoinColumn(name = "sucursal_id", nullable = false)
    private Sucursal sucursal;

    @ManyToOne
    @JoinColumn(name = "almacenero_id")
    private Usuario almacenero;

    private String estado; // PENDIENTE, ASIGNADO, EN_PROCESO, ENTREGADO, CANCELADO
    private String clienteNombre;
    private String observaciones;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaAsignacion;
    private LocalDateTime fechaEntrega;
}