package com.tato.motorepuestos.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 15)
    private String tipoDocumento;

    @Column(nullable = false, unique = true, length = 20)
    private String numeroDocumento;

    @Column(nullable = false)
    private String razonSocialNombre;

    @Column(unique = true)
    private String correo;

    private String password;

    private String telefono;
    private String direccion;

    private Boolean activo = true;

    @Column(updatable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    public Cliente() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }

    public String getRazonSocialNombre() { return razonSocialNombre; }
    public void setRazonSocialNombre(String razonSocialNombre) { this.razonSocialNombre = razonSocialNombre; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}