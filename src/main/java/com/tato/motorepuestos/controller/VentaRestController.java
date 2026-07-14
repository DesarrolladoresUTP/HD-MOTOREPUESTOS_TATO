package com.tato.motorepuestos.controller;

import com.tato.motorepuestos.model.Cliente;
import com.tato.motorepuestos.model.DetalleVenta;
import com.tato.motorepuestos.model.Venta;
import com.tato.motorepuestos.repository.ClienteRepository;
import com.tato.motorepuestos.repository.VentaRepository;
import com.tato.motorepuestos.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ventas")
public class VentaRestController {

    @Autowired private VentaService ventaService;
    @Autowired private VentaRepository ventaRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private VentaPdfService ventaPdfService;
    @Autowired private PdfService pdfService;
    @Autowired private EmailService emailService;
    @Autowired private HistorialService historialService;
    @Autowired private CajaService cajaService;

    @GetMapping
    public List<Venta> listar(HttpSession session) {
        Long sucursalId = (Long) session.getAttribute("sucursalId");
        return ventaService.listarPorSucursal(sucursalId);
    }

    @GetMapping("/correlativo")
    public ResponseEntity<String> obtenerSiguienteCorrelativo(
            @RequestParam String tipo,
            @RequestParam String serie) {
        String ultimo = ventaRepository.findUltimoCorrelativo(tipo, serie);
        int siguiente = 1;
        if (ultimo != null && !ultimo.isEmpty()) {
            try { siguiente = Integer.parseInt(ultimo) + 1; } catch (Exception e) {}
        }
        return ResponseEntity.ok(String.format("%08d", siguiente));
    }

    @PostMapping
    public ResponseEntity<?> registrarVenta(@RequestBody Map<String, Object> payload,
                                            HttpSession session) {
        Long usuarioId  = (Long) session.getAttribute("usuarioId");
        Long sucursalId = (Long) session.getAttribute("sucursalId");

        // Validar que haya una caja activa para este usuario
        if (cajaService.obtenerCajaActiva(usuarioId, sucursalId) == null) {
            return ResponseEntity.badRequest().body("CAJA_CERRADA");
        }

        try {
            String docCliente = payload.getOrDefault("documentoCliente", "00000000").toString().trim();
            String nomCliente = payload.getOrDefault("nombreCliente", "Público General").toString().trim();

            if (!docCliente.isEmpty() && !docCliente.equals("00000000")) {
                clienteRepository.findByNumeroDocumento(docCliente).orElseGet(() -> {
                    Cliente nuevo = new Cliente();
                    nuevo.setNumeroDocumento(docCliente);
                    nuevo.setRazonSocialNombre(nomCliente.isEmpty() ? "Sin nombre" : nomCliente.toUpperCase());
                    nuevo.setTipoDocumento(docCliente.length() == 8 ? "DNI" : "RUC");
                    nuevo.setActivo(false);
                    return clienteRepository.save(nuevo);
                });
            }

            payload.put("tipoComprobante", "Nota de Venta");
            payload.put("serie", "T001");
            payload.put("numeroComprobante",
                    String.format("%08d", System.currentTimeMillis() % 100000000));

            com.tato.motorepuestos.model.Venta venta = ventaService.registrarVenta(payload, usuarioId, sucursalId);
            boolean esperaAlmacen = "PENDIENTE_ALMACEN".equals(venta.getEstadoVenta());
            return ResponseEntity.ok(Map.of(
                    "ventaId", venta.getId(),
                    "esperaAlmacen", esperaAlmacen
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/proforma")
    public ResponseEntity<?> enviarProforma(@RequestBody Map<String, Object> payload) {
        try {
            String correo = payload.get("correoCliente").toString();
            Object nombreObj = payload.get("nombreCliente");
            String nombreCliente = (nombreObj != null && !nombreObj.toString().trim().isEmpty())
                    ? nombreObj.toString().trim() : "Público General";
            Object docObj = payload.get("documentoCliente");
            String docCliente = (docObj != null && !docObj.toString().trim().isEmpty())
                    ? docObj.toString().trim() : "00000000";
            payload.put("documentoCliente", docCliente);
            payload.put("nombreCliente", nombreCliente);

            byte[] pdfBytes = pdfService.generarComprobantePdf(payload, true);
            String cuerpo = "<h3>Hola " + nombreCliente + "</h3>"
                    + "<p>Adjuntamos la Nota de Venta de su compra.</p>"
                    + "<p>Monto: <b>S/ " + payload.get("total") + "</b></p>";
            emailService.enviarCorreoConPdf(correo,
                    "Nota de Venta - Motorepuestos Tato", cuerpo, pdfBytes,
                    "Nota_De_Venta.pdf");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/emitir")
    public ResponseEntity<?> emitirComprobante(@PathVariable Long id,
                                               @RequestBody Map<String, String> payload) {
        try {
            Venta venta = ventaRepository.findById(id).orElseThrow();
            venta.setTipoComprobante(payload.get("tipoDocumento"));
            venta.setSerie(payload.get("serie"));
            venta.setNumeroComprobante(payload.get("numero"));
            venta.setEstadoSunat("PENDIENTE");

            String docCliente    = payload.get("documentoCliente");
            String nombreCliente = payload.get("nombreCliente");
            if (docCliente != null && !docCliente.trim().isEmpty()) {
                Cliente clienteActualizado = clienteRepository
                        .findByNumeroDocumento(docCliente.trim())
                        .orElseGet(() -> {
                            Cliente nuevo = new Cliente();
                            nuevo.setNumeroDocumento(docCliente.trim());
                            nuevo.setRazonSocialNombre(
                                    nombreCliente != null ? nombreCliente.trim() : "Sin nombre");
                            nuevo.setTipoDocumento(
                                    docCliente.trim().length() == 8 ? "DNI" : "RUC");
                            return clienteRepository.save(nuevo);
                        });
                venta.setCliente(clienteActualizado);
            }
            ventaRepository.save(venta);

            Cliente c = venta.getCliente();
            String desc = "Emisión " + venta.getTipoComprobante()
                    + " " + venta.getSerie() + "-" + venta.getNumeroComprobante()
                    + " | Cliente: " + (c != null ? c.getRazonSocialNombre() : "Público General");
            historialService.registrarAccion("Ventas", "Emision de Comprobante",
                    desc, venta.getUsuario().getId(), venta.getSucursal().getId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/sunat")
    public ResponseEntity<?> enviarSunat(@PathVariable Long id) {
        try {
            Venta venta = ventaRepository.findById(id).orElseThrow();
            Thread.sleep(1500);
            venta.setEstadoSunat("ACEPTADO");
            ventaRepository.save(venta);
            String desc = "Envío a SUNAT Aceptado | "
                    + venta.getTipoComprobante() + " "
                    + venta.getSerie() + "-" + venta.getNumeroComprobante();
            historialService.registrarAccion("Ventas", "Envío a SUNAT",
                    desc, venta.getUsuario().getId(), venta.getSucursal().getId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/enviar-cliente")
    public ResponseEntity<?> enviarDocumentosCliente(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        try {
            Venta venta = ventaRepository.findById(id).orElseThrow();
            String correo = (payload.get("correoCliente") != null) ? payload.get("correoCliente").toString().trim() : venta.getCorreoCliente();

            if (correo == null || correo.isEmpty()) return ResponseEntity.badRequest().body("Debe proporcionar un correo.");

            // USAMOS EL NUEVO SERVICIO PARA EL PDF
            byte[] pdfBytes = ventaPdfService.generarPdfVenta(venta);

            String docNombre = venta.getSerie() + "-" + venta.getNumeroComprobante();
            String cuerpo = "<h3>¡Gracias por su compra en Motorepuestos Tato!</h3><p>Adjuntamos su comprobante electrónico.</p>";

            emailService.enviarCorreoConArchivos(correo, "Comprobante " + docNombre, cuerpo,
                    pdfBytes, docNombre + ".pdf", null, null, null, null);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> anularVenta(@PathVariable Long id, HttpSession session) {
        try {
            Venta venta = ventaRepository.findById(id).orElseThrow();
            venta.setEstadoVenta("ANULADA");
            ventaRepository.save(venta);

            Long usuarioId  = (Long) session.getAttribute("usuarioId");
            Long sucursalId = (Long) session.getAttribute("sucursalId");
            historialService.registrarAccion("Ventas", "Anulación",
                    "Se anuló la venta " + venta.getSerie()
                            + "-" + venta.getNumeroComprobante(),
                    usuarioId, sucursalId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<?> descargarPdf(@PathVariable Long id) {
        try {
            Venta venta = ventaRepository.findById(id).orElseThrow();

            // AQUÍ ESTÁ LA CLAVE: Llamamos al servicio con el diseño profesional
            byte[] pdfBytes = ventaPdfService.generarPdfVenta(venta);

            String nombreArchivo = (venta.getSerie() != null ? venta.getSerie() + "-" + venta.getNumeroComprobante() : "Venta-" + venta.getId()) + ".pdf";

            return ResponseEntity.ok()
                    .header("Content-Type", "application/pdf")
                    .header("Content-Disposition", "inline; filename=\"" + nombreArchivo + "\"")
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/caja-activa")
    public ResponseEntity<?> ventasDeCajaActiva(HttpSession session) {
        Long usuarioId  = (Long) session.getAttribute("usuarioId");
        Long sucursalId = (Long) session.getAttribute("sucursalId");
        if (usuarioId == null) return ResponseEntity.status(401).build();

        com.tato.motorepuestos.model.Caja caja = cajaService.obtenerCajaActiva(usuarioId, sucursalId);
        if (caja == null) return ResponseEntity.ok(List.of());

        List<Venta> ventas = ventaRepository.findBySucursalIdOrderByFechaDesc(sucursalId)
                .stream()
                .filter(v -> v.getUsuario().getId().equals(usuarioId))
                .filter(v -> !v.getFecha().isBefore(caja.getFechaApertura()))
                .collect(java.util.stream.Collectors.toList());

        List<Map<String, Object>> resultado = ventas.stream().map(v -> {
            Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("id", v.getId());
            m.put("numeroComprobante", v.getSerie() + "-" + v.getNumeroComprobante());
            m.put("fecha", v.getFecha().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            m.put("cliente", v.getCliente() != null ? v.getCliente().getRazonSocialNombre() : "Público General");
            m.put("metodoPago", v.getMetodoPago());
            m.put("total", v.getTotal());
            m.put("estadoVenta", v.getEstadoVenta());
            m.put("estadoSunat", v.getEstadoSunat());
            m.put("productos", v.getDetalles().stream().map(d ->
                    d.getCantidad() + "x " + d.getProducto().getNombre()
            ).collect(java.util.stream.Collectors.joining(", ")));
            return m;
        }).collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(resultado);
    }

    @Autowired
    private com.tato.motorepuestos.service.AlmacenComplementarioService almacenComplementarioService;

    @GetMapping("/{id}/estado-almacen")
    public ResponseEntity<?> estadoAlmacen(@PathVariable Long id) {
        String estado = almacenComplementarioService.getEstadoPedidoPorVenta(id);
        return ResponseEntity.ok(Map.of("estado", estado != null ? estado : "N/A"));
    }
}