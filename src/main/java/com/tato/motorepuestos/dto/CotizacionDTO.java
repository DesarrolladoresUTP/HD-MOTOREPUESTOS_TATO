package com.tato.motorepuestos.dto;
import java.util.List;

public class CotizacionDTO {
    private String nombreCliente;
    private String correoCliente;
    private List<ItemDTO> carrito;
    private String documentoCliente;

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }
    public String getDocumentoCliente() { return documentoCliente; }
    public void setDocumentoCliente(String documentoCliente) { this.documentoCliente = documentoCliente; }
    public String getCorreoCliente() { return correoCliente; }
    public void setCorreoCliente(String correoCliente) { this.correoCliente = correoCliente; }
    public List<ItemDTO> getCarrito() { return carrito; }
    public void setCarrito(List<ItemDTO> carrito) { this.carrito = carrito; }

    public static class ItemDTO {
        private Long id;
        private String nombre;
        private Integer cantidad;
        private Double precio;
        public Long getId() { return id; } public void setId(Long id) { this.id = id; }
        public String getNombre() { return nombre; } public void setNombre(String nombre) { this.nombre = nombre; }
        public Integer getCantidad() { return cantidad; } public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
        public Double getPrecio() { return precio; } public void setPrecio(Double precio) { this.precio = precio; }
    }
}