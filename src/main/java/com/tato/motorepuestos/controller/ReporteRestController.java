package com.tato.motorepuestos.controller;

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
                reporteGlobal.add(map);
            }

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
                reporteGlobal.add(map);
            }

            return ResponseEntity.ok(reporteGlobal);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error construyendo el reporte: " + e.getMessage());
        }
    }
}