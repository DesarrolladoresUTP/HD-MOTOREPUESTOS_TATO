package com.tato.motorepuestos.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos_web")
public class PedidoWeb {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_cliente_id")
    @JsonIgnore
    private UsuarioCliente usuarioCliente;

    private String tipoDocumento;
    private String numeroDocumento;
    private String nombreCompleto;
    private String telefono;

    private String metodoEntrega;
    private String direccionEntrega;

    @Column(nullable = false)
    private String estado = "PENDIENTE";

    @Column(nullable = false)
    private BigDecimal total;

    @Column(updatable = false)
    private LocalDateTime fechaPedido;

    @OneToMany(mappedBy = "pedidoWeb", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedidoWeb> detalles = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.fechaPedido = LocalDateTime.now();
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public UsuarioCliente getUsuarioCliente() { return usuarioCliente; }
    public void setUsuarioCliente(UsuarioCliente usuarioCliente) { this.usuarioCliente = usuarioCliente; }
    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getMetodoEntrega() { return metodoEntrega; }
    public void setMetodoEntrega(String metodoEntrega) { this.metodoEntrega = metodoEntrega; }
    public String getDireccionEntrega() { return direccionEntrega; }
    public void setDireccionEntrega(String direccionEntrega) { this.direccionEntrega = direccionEntrega; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public LocalDateTime getFechaPedido() { return fechaPedido; }
    public void setFechaPedido(LocalDateTime fechaPedido) { this.fechaPedido = fechaPedido; }
    public List<DetallePedidoWeb> getDetalles() { return detalles; }
    public void setDetalles(List<DetallePedidoWeb> detalles) { this.detalles = detalles; }

    public void addDetalle(DetallePedidoWeb detalle) {
        detalles.add(detalle);
        detalle.setPedidoWeb(this);
    }
}