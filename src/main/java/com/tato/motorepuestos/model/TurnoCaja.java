package com.tato.motorepuestos.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "turnos_caja")
@Data
public class TurnoCaja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fechaApertura;

    private LocalDateTime fechaCierre;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montoInicial;

    @Column(precision = 10, scale = 2)
    private BigDecimal ingresosEfectivo = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal montoFinalCalculado = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal montoFinalReal;

    @Column(precision = 10, scale = 2)
    private BigDecimal diferencia;

    @Column(nullable = false)
    private String estado = "ABIERTA";

    private String observaciones;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sucursal_id", nullable = false)
    private Sucursal sucursal;
}