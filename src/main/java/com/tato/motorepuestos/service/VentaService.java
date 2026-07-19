package com.tato.motorepuestos.service;

import com.tato.motorepuestos.model.*;
import com.tato.motorepuestos.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class VentaService {

    @Autowired
    private VentaRepository ventaRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private SucursalRepository sucursalRepository;
    @Autowired
    private InventarioSucursalRepository inventarioRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private HistorialService historialService;

    @Autowired
    private MovimientoInventarioRepository movimientoRepository;

    @Autowired
    private AlmacenComplementarioService almacenComplementarioService;

    @Transactional
    public Venta registrarVenta(Map<String, Object> payload, Long usuarioId, Long sucursalId) {
        String numeroDocumento = payload.get("documentoCliente").toString();
        Cliente cliente = clienteRepository.findByNumeroDocumento(numeroDocumento)
                .orElseThrow(() -> new RuntimeException(
                        "Cliente no encontrado. Debe consultarse primero antes de registrar la venta"));

        boolean esperaAlmacen = almacenComplementarioService.requiereConfirmacionAlmacenero(sucursalId);

        Venta venta = new Venta();
        venta.setFecha(LocalDateTime.now());
        venta.setTipoComprobante(payload.get("tipoComprobante").toString());
        venta.setSerie(payload.get("serie").toString());
        venta.setNumeroComprobante(payload.get("numeroComprobante").toString());
        venta.setCliente(cliente);

        if (payload.containsKey("correoCliente") && payload.get("correoCliente") != null
                && !payload.get("correoCliente").toString().isEmpty()) {
            venta.setCorreoCliente(payload.get("correoCliente").toString());
        }

        venta.setMetodoPago(payload.get("metodoPago").toString());
        venta.setSubtotal(new BigDecimal(payload.get("subtotal").toString()));
        venta.setIgv(new BigDecimal(payload.get("igv").toString()));
        venta.setTotal(new BigDecimal(payload.get("total").toString()));

        venta.setEstadoVenta(esperaAlmacen ? "PENDIENTE_ALMACEN" : "REGISTRADA");
        venta.setEstadoSunat("SIN EMITIR");

        Usuario cajero = usuarioRepository.findById(usuarioId).orElseThrow();
        String nombreCajero = cajero.getNombres() + " " + cajero.getApellidos();

        venta.setUsuario(cajero);
        venta.setSucursal(sucursalRepository.findById(sucursalId).orElseThrow());

        List<Map<String, Object>> items = (List<Map<String, Object>>) payload.get("items");
        if (items == null || items.isEmpty()) {
            throw new RuntimeException("La venta debe tener al menos un producto");
        }

        for (Map<String, Object> item : items) {
            Long inventarioId = Long.valueOf(item.get("inventarioId").toString());
            Integer cantidad = Integer.valueOf(item.get("cantidad").toString());

            InventarioSucursal inventario = inventarioRepository.findById(inventarioId)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado en inventario"));

            if (!Boolean.TRUE.equals(inventario.getActivo())) {
                throw new RuntimeException("El producto " + inventario.getProducto().getNombre()
                        + " no está disponible");
            }

            if (inventario.getStock() < cantidad) {
                throw new RuntimeException("¡Venta detenida! El producto '"
                        + inventario.getProducto().getNombre()
                        + "' acaba de ser comprado por otro canal o no tiene stock suficiente. Disponible: "
                        + inventario.getStock());
            }

            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(venta);
            detalle.setProducto(inventario.getProducto());
            detalle.setCantidad(cantidad);
            detalle.setPrecioUnitario(new BigDecimal(item.get("precioUnitario").toString()));
            detalle.setSubtotal(new BigDecimal(item.get("importe").toString()));
            venta.getDetalles().add(detalle);

            if (!esperaAlmacen) {
                Integer stockAnterior = inventario.getStock();

                inventario.setStock(stockAnterior - cantidad);
                int vendidas = inventario.getUnidadesVendidas() == null ? 0 : inventario.getUnidadesVendidas();
                inventario.setUnidadesVendidas(vendidas + cantidad);
                inventarioRepository.save(inventario);

                MovimientoInventario mov = new MovimientoInventario();
                mov.setInventario(inventario);
                mov.setTipoMovimiento("Venta en POS");
                mov.setStockAnterior(stockAnterior);
                mov.setStockNuevo(inventario.getStock());
                mov.setObservacion("Comp. " + venta.getSerie() + "-" + venta.getNumeroComprobante() + " | Cliente: " + cliente.getRazonSocialNombre());
                mov.setFechaRegistro(LocalDateTime.now());
                mov.setNombreUsuario(nombreCajero);
                movimientoRepository.save(mov);
            }
        }

        ventaRepository.save(venta);

        String desc = "Venta " + venta.getSerie() + "-" + venta.getNumeroComprobante()
                + " | Cliente: " + cliente.getRazonSocialNombre()
                + " | Total: S/ " + venta.getTotal().toPlainString()
                + (esperaAlmacen ? " | Pendiente de entrega por almacen" : "");
        historialService.registrarAccion("Ventas", "Salida de Mercadería", desc, usuarioId, sucursalId);

        if (esperaAlmacen) {
            almacenComplementarioService.crearPedidoAlmacenParaVenta(venta);
        }

        return venta;
    }

    public List<Venta> listarPorSucursal(Long sucursalId) {
        return ventaRepository.findBySucursalIdOrderByFechaDesc(sucursalId);
    }
}