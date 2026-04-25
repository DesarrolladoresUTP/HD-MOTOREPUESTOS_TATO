package com.tato.motorepuestos.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "compras")
@Data
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate fechaEmision;

    private LocalDate fechaVencimiento;

    @Column(nullable = false)
    private String rucProveedor;

    @Column(nullable = false)
    private String razonSocialProveedor;

    @Column(nullable = false)
    private String tipoDocumento;

    @Column(nullable = false)
    private String serie;

    @Column(nullable = false)
    private String numeroComprobante;

    @Column(nullable = false)
    private String condicionPago;

    @Column(nullable = false)
    private String moneda;

    @Column(precision = 10, scale = 3)
    private BigDecimal tipoCambio;

    private Boolean incluyeIgv = true;

    @Column(length = 500)
    private String observacion;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal igv;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @ManyToOne
    @JoinColumn(name = "sucursal_id", nullable = false)
    private Sucursal sucursal;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
}