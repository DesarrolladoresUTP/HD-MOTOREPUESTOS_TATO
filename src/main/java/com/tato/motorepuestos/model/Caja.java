package com.tato.motorepuestos.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cajas")
@Data
public class Caja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sucursal_id", nullable = false)
    private Sucursal sucursal;

    @Column(nullable = false)
    private LocalDateTime fechaApertura;

    private LocalDateTime fechaCierre;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montoInicial = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal montoEsperado;

    @Column(precision = 10, scale = 2)
    private BigDecimal montoReal;

    @Column(precision = 10, scale = 2)
    private BigDecimal diferencia;

    @Column(length = 500)
    private String observaciones;

    @Column(nullable = false, length = 20)
    private String estado = "ABIERTA";
}