package com.tato.motorepuestos.controller;

import com.tato.motorepuestos.model.DetallePedidoWeb;
import com.tato.motorepuestos.model.DetalleVenta;
import com.tato.motorepuestos.model.PedidoWeb;
import com.tato.motorepuestos.model.Venta;
import com.tato.motorepuestos.repository.PedidoWebRepository;
import com.tato.motorepuestos.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reportes")
public class ReporteRestController {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private PedidoWebRepository pedidoWebRepository;

    @GetMapping("/ventas-globales")
    public ResponseEntity<?> obtenerReporteGlobal() {
        List<Map<String, Object>> reporteGlobal = new ArrayList<>();

        try {
            // Procesar Ventas de Tienda Física
            List<Venta> ventas = ventaRepository.findAll();
            for (Venta v : ventas) {
                Map<String, Object> map = new HashMap<>();
                map.put("origen", "LOCAL");
                map.put("idOriginal", v.getId());

                String codigoFinal = "V-" + v.getId();
                if ("SIN EMITIR".equalsIgnoreCase(v.getEstadoSunat())) {
                    codigoFinal = "TICKET PENDIENTE (" + v.getNumeroComprobante() + ")";
                }
                else if (v.getSerie() != null && v.getNumeroComprobante() != null) {
                    codigoFinal = v.getSerie() + "-" + v.getNumeroComprobante();
                }
                else if (v.getNumeroComprobante() != null) {
                    codigoFinal = v.getNumeroComprobante();
                }
                map.put("codigo", codigoFinal);
                map.put("fecha", v.getFecha() != null ? v.getFecha().toString() : "");

                String nombreCliente = "Mostrador";
                if (v.getCliente() != null && v.getCliente().getRazonSocialNombre() != null) {
                    nombreCliente = v.getCliente().getRazonSocialNombre();
                }
                map.put("cliente", nombreCliente);
                map.put("total", v.getTotal());
                map.put("estado", v.getEstadoVenta() != null ? v.getEstadoVenta() : "COMPLETADO");

                // NUEVO: Agregamos los detalles de los productos para ventas locales
                if (v.getDetalles() != null) {
                    List<Map<String, Object>> detallesList = new ArrayList<>();
                    for (DetalleVenta dv : v.getDetalles()) {
                        Map<String, Object> detalleMap = new HashMap<>();
                        // Asumiendo que DetalleVenta tiene relación con Producto o almacena el nombre
                        detalleMap.put("nombreProducto", dv.getProducto() != null ? dv.getProducto().getNombre() : "Producto");
                        detalleMap.put("cantidad", dv.getCantidad());
                        detalleMap.put("precioUnitario", dv.getPrecioUnitario());
                        detalleMap.put("subtotal", dv.getSubtotal());
                        detallesList.add(detalleMap);
                    }
                    map.put("detalles", detallesList);
                }

                reporteGlobal.add(map);
            }

            // Procesar Pedidos Web
            List<PedidoWeb> pedidos = pedidoWebRepository.findAll();
            for (PedidoWeb p : pedidos) {
                Map<String, Object> map = new HashMap<>();
                map.put("origen", "WEB");
                map.put("idOriginal", p.getId());
                map.put("codigo", "WEB-" + String.format("%04d", p.getId()));
                map.put("fecha", p.getFechaPedido() != null ? p.getFechaPedido().toString() : "");
                map.put("cliente", p.getNombreCompleto() != null ? p.getNombreCompleto() : "Cliente Web");
                map.put("total", p.getTotal());
                map.put("estado", p.getEstado());

                // NUEVO: Agregamos los detalles de los productos para pedidos web
                if (p.getDetalles() != null) {
                    List<Map<String, Object>> detallesList = new ArrayList<>();
                    for (DetallePedidoWeb dpw : p.getDetalles()) {
                        Map<String, Object> detalleMap = new HashMap<>();
                        detalleMap.put("nombreProducto", dpw.getNombreProducto());
                        detalleMap.put("cantidad", dpw.getCantidad());
                        detalleMap.put("precioUnitario", dpw.getPrecioUnitario());
                        detalleMap.put("subtotal", dpw.getSubtotal());
                        detallesList.add(detalleMap);
                    }
                    map.put("detalles", detallesList);
                }

                reporteGlobal.add(map);
            }

            return ResponseEntity.ok(reporteGlobal);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error construyendo el reporte: " + e.getMessage());
        }
    }
}