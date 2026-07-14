package com.tato.motorepuestos.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "detalle_pedido_almacen")
@Data
public class DetallePedidoAlmacen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pedido_almacen_id", nullable = false)
    private PedidoAlmacen pedidoAlmacen;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    private Integer cantidad;
    private String ubicacionSnapshot;
    private Boolean recogido = false;
}