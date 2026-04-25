package com.tato.motorepuestos.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "productos")
@Data
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_interno", nullable = false, unique = true)
    private String codigoInterno;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(length = 1000)
    private String descripcion;

    @Column(nullable = false)
    private String marca;

    private String imagen;

    @ManyToOne
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

}