package com.tato.motorepuestos.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios_clientes")
public class UsuarioCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String tipoDocumento;

    @Column(nullable = false, length = 20, unique = true)
    private String numeroDocumento;

    @Column(nullable = false, length = 150)
    private String nombreCompleto;

    @Column(nullable = false, length = 100, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String tokenRecuperacion;

    private Boolean activo = true;

    @Column(updatable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    protected void onCreate() {
        this.fechaRegistro = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getTokenRecuperacion() { return tokenRecuperacion; }
    public void setTokenRecuperacion(String tokenRecuperacion) { this.tokenRecuperacion = tokenRecuperacion; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}