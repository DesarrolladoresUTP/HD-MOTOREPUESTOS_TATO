package com.tato.motorepuestos.controller;

import com.tato.motorepuestos.dto.PedidoWebDTO;
import com.tato.motorepuestos.model.PedidoWeb;
import com.tato.motorepuestos.service.PdfService;
import com.tato.motorepuestos.service.PedidoWebService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pedidos-web")
public class PedidoWebRestController {

    @Autowired
    private PedidoWebService pedidoService;

    @Autowired
    private PdfService pdfService;

    @PostMapping("/procesar")
    public ResponseEntity<?> procesarPedido(@RequestBody PedidoWebDTO pedidoDTO, HttpSession session) {
        try {
            Long clienteWebId = (Long) session.getAttribute("clienteWebId");
            PedidoWeb nuevoPedido = pedidoService.procesarPedido(pedidoDTO, clienteWebId);
            return ResponseEntity.ok(nuevoPedido);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al procesar pedido: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<PedidoWeb>> listarPedidos() {
        return ResponseEntity.ok(pedidoService.listarTodos());
    }

    @GetMapping("/mis-pedidos")
    public ResponseEntity<?> obtenerMisPedidos(HttpSession session) {
        Long clienteWebId = (Long) session.getAttribute("clienteWebId");
        if (clienteWebId == null) {
            return ResponseEntity.status(401).body("Debes iniciar sesión para ver esta información.");
        }
        return ResponseEntity.ok(pedidoService.listarMisPedidos(clienteWebId));
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id, @RequestParam String estado) {
        try {
            pedidoService.actualizarEstado(id, estado);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/boleta-pdf")
    public ResponseEntity<?> descargarBoletaPdf(@PathVariable Long id, HttpSession session) {
        Long clienteWebId = (Long) session.getAttribute("clienteWebId");
        if (clienteWebId == null) {
            return ResponseEntity.status(401).body("No autenticado");
        }
        try {
            PedidoWeb pedido = pedidoService.obtenerPorId(id); // necesitas este método
            if (pedido == null) return ResponseEntity.notFound().build();
            // Verificar que el pedido pertenece al cliente
            if (pedido.getUsuarioCliente() == null ||
                    !pedido.getUsuarioCliente().getId().equals(clienteWebId)) {
                return ResponseEntity.status(403).body("Acceso denegado");
            }
            Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("tipoComprobante", pedido.getTipoDocumento());
            payload.put("serie", "WEB");
            payload.put("numeroComprobante", String.format("%05d", pedido.getId()));
            payload.put("nombreCliente", pedido.getNombreCompleto());
            payload.put("documentoCliente", pedido.getNumeroDocumento());
            payload.put("metodoPago", "Tienda Online");
            payload.put("total", pedido.getTotal().toString());
            List<Map<String, Object>> items = pedido.getDetalles().stream().map(d -> {
                Map<String, Object> item = new java.util.HashMap<>();
                item.put("codigo", "-");
                item.put("nombre", d.getNombreProducto());
                item.put("cantidad", String.valueOf(d.getCantidad()));
                item.put("importe", d.getSubtotal().toString());
                return item;
            }).collect(java.util.stream.Collectors.toList());
            payload.put("items", items);
            byte[] pdf = pdfService.generarComprobantePdf(payload, false);
            return ResponseEntity.ok()
                    .header("Content-Disposition",
                            "attachment; filename=boleta-web-" + id + ".pdf")
                    .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error generando PDF: " + e.getMessage());
        }
    }
}