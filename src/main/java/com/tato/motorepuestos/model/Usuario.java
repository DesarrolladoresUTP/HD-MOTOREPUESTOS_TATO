package com.tato.motorepuestos.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "usuarios")
@Data
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombres;

    @Column(nullable = false)
    private String apellidos;

    @Column(nullable = false, unique = true)
    private String correoElectronico;

    @Column(nullable = false)
    private String password;

    private String foto;

    private Boolean activo = true;

    @ManyToOne
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;
}