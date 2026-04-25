package com.tato.motorepuestos.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "clientes")
@Data
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 11)
    private String tipoDocumento;   // sin unique

    @Column(nullable = false, unique = true, length = 15)
    private String numeroDocumento;

    @Column(nullable = false)
    private String razonSocialNombre;

    private String direccion;

    @Column(length = 20)
    private String telefono;
}