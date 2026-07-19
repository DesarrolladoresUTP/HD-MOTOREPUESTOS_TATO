package com.tato.motorepuestos.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimientos_inventario")
@Data
public class MovimientoInventario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventario_id", nullable = false)
    @JsonIgnore
    private InventarioSucursal inventario;

    private String tipoMovimiento; // Ej: "Ajuste Manual", "Venta", "Compra"
    private Integer stockAnterior;
    private Integer stockNuevo;
    private String observacion; // Motivo del ajuste

    @JsonFormat(pattern = "dd/MM/yyyy hh:mm a")
    private LocalDateTime fechaRegistro;

    private String nombreUsuario; // Quien hizo el cambio
}