package com.tato.motorepuestos.service;

import com.tato.motorepuestos.model.*;
import com.tato.motorepuestos.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class CompraService {

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private DetalleCompraRepository detalleCompraRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private InventarioSucursalRepository inventarioRepository;

    @Autowired
    private SucursalRepository sucursalRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private HistorialService historialService;

    public List<Compra> listarPorSucursal(Long sucursalId) {
        return compraRepository.findBySucursalIdOrderByFechaEmisionDesc(sucursalId);
    }

    @Transactional
    public void registrarCompra(Map<String, Object> payload, Long usuarioId, Long sucursalId) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow();
        Sucursal sucursal = sucursalRepository.findById(sucursalId).orElseThrow();

        Compra compra = new Compra();
        compra.setFechaEmision(LocalDate.parse(payload.get("fechaEmision").toString()));

        if (payload.get("fechaVencimiento") != null
                && !payload.get("fechaVencimiento").toString().isEmpty()) {
            compra.setFechaVencimiento(LocalDate.parse(payload.get("fechaVencimiento").toString()));
        }

        compra.setRucProveedor(payload.get("rucProveedor").toString());
        compra.setRazonSocialProveedor(payload.get("razonSocialProveedor").toString());
        compra.setTipoDocumento(payload.get("tipoDocumento").toString());
        compra.setSerie(payload.get("serie").toString());
        compra.setNumeroComprobante(payload.get("numeroComprobante").toString());
        compra.setCondicionPago(payload.get("condicionPago").toString());
        compra.setMoneda(payload.get("moneda").toString());

        if (payload.get("tipoCambio") != null && !payload.get("tipoCambio").toString().isEmpty()) {
            compra.setTipoCambio(new BigDecimal(payload.get("tipoCambio").toString()));
        }

        compra.setIncluyeIgv((Boolean) payload.get("incluyeIgv"));
        compra.setObservacion(payload.get("observacion") != null
                ? payload.get("observacion").toString() : "");
        compra.setSubtotal(new BigDecimal(payload.get("subtotal").toString()));
        compra.setIgv(new BigDecimal(payload.get("igv").toString()));
        compra.setTotal(new BigDecimal(payload.get("total").toString()));
        compra.setUsuario(usuario);
        compra.setSucursal(sucursal);

        Compra compraGuardada = compraRepository.save(compra);

        List<Map<String, Object>> items = (List<Map<String, Object>>) payload.get("items");
        for (Map<String, Object> item : items) {
            Long productoId = Long.parseLong(item.get("productoId").toString());
            Integer cantidad = Integer.parseInt(item.get("cantidad").toString());
            BigDecimal precioUnitario = new BigDecimal(item.get("precioUnitario").toString());
            BigDecimal importe = new BigDecimal(item.get("importe").toString());

            Producto producto = productoRepository.findById(productoId).orElseThrow();

            DetalleCompra detalle = new DetalleCompra();
            detalle.setCompra(compraGuardada);
            detalle.setProducto(producto);
            detalle.setCantidad(cantidad);
            detalle.setPrecioUnitario(precioUnitario);
            detalle.setSubtotal(importe);
            detalleCompraRepository.save(detalle);

            InventarioSucursal inventario = inventarioRepository
                    .findByProductoIdAndSucursalId(productoId, sucursalId)
                    .orElseThrow(() -> new RuntimeException(
                            "El producto no está registrado en esta sucursal. "
                                    + "Agrégalo primero desde el catálogo de productos"));

            inventario.setStock(inventario.getStock() + cantidad);
            inventario.setPrecioCompra(precioUnitario);
            inventarioRepository.save(inventario);
        }

        historialService.registrarAccion(
                "Compras", "Ingreso de Mercadería",
                "Ingreso " + compraGuardada.getSerie() + "-"
                        + compraGuardada.getNumeroComprobante()
                        + " | Proveedor: " + compraGuardada.getRazonSocialProveedor(),
                usuarioId, sucursalId
        );
    }
}