package com.tato.motorepuestos.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "config_almacen")
@Data
public class ConfigAlmacen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sucursal_id", nullable = false)
    private Sucursal sucursal;

    private Integer totalEstantes;
    private Integer totalFilas;
    private Integer totalColumnas;

    private Boolean requiereConfirmacionAlmacenero = false;
}