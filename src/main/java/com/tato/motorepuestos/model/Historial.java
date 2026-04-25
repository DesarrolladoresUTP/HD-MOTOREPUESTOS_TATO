package com.tato.motorepuestos.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "historial")
@Data
public class Historial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String modulo;

    @Column(nullable = false)
    private String accion;

    @Column(length = 1000, nullable = false)
    private String descripcion;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "sucursal_id")
    private Sucursal sucursal;
}