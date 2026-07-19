package com.tato.motorepuestos.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "traslados")
public class Traslado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sucursal_origen_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Sucursal sucursalOrigen;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sucursal_destino_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Sucursal sucursalDestino;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"}) // Ocultamos el password por seguridad
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDateTime fechaRegistro;

    @OneToMany(mappedBy = "traslado", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleTraslado> detalles = new ArrayList<>();

    // --- Getters y Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Sucursal getSucursalOrigen() { return sucursalOrigen; }
    public void setSucursalOrigen(Sucursal sucursalOrigen) { this.sucursalOrigen = sucursalOrigen; }

    public Sucursal getSucursalDestino() { return sucursalDestino; }
    public void setSucursalDestino(Sucursal sucursalDestino) { this.sucursalDestino = sucursalDestino; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public List<DetalleTraslado> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleTraslado> detalles) { this.detalles = detalles; }
}