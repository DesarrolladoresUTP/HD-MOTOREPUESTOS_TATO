package com.tato.motorepuestos.controller;

import com.tato.motorepuestos.model.Cliente;
import com.tato.motorepuestos.model.DetalleVenta;
import com.tato.motorepuestos.model.Venta;
import com.tato.motorepuestos.repository.ClienteRepository;
import com.tato.motorepuestos.repository.VentaRepository;
import com.tato.motorepuestos.service.EmailService;
import com.tato.motorepuestos.service.HistorialService;
import com.tato.motorepuestos.service.PdfService;
import com.tato.motorepuestos.service.VentaService;
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

    @Autowired
    private VentaService ventaService;

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PdfService pdfService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private HistorialService historialService;

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
            try {
                siguiente = Integer.parseInt(ultimo) + 1;
            } catch (Exception e) { }
        }
        return ResponseEntity.ok(String.format("%08d", siguiente));
    }

    @PostMapping
    public ResponseEntity<?> registrarVenta(@RequestBody Map<String, Object> payload,
                                            HttpSession session) {
        Long usuarioId  = (Long) session.getAttribute("usuarioId");
        Long sucursalId = (Long) session.getAttribute("sucursalId");
        try {
            payload.put("tipoComprobante", "Nota de Venta");
            payload.put("serie", "T001");
            payload.put("numeroComprobante",
                    String.format("%08d", System.currentTimeMillis() % 100000000));
            ventaService.registrarVenta(payload, usuarioId, sucursalId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/proforma")
    public ResponseEntity<?> enviarProforma(@RequestBody Map<String, Object> payload) {
        try {
            String correo = payload.get("correoCliente").toString();
            System.out.println("ITEMS RECIBIDOS: " + payload.get("items"));

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
            historialService.registrarAccion("Ventas", "Emisión de Comprobante",
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
    public ResponseEntity<?> enviarDocumentosCliente(@PathVariable Long id,
                                                     @RequestBody Map<String, Object> payload) {
        try {
            Venta venta = ventaRepository.findById(id).orElseThrow();

            String correo = null;
            Object correoObj = payload.get("correoCliente");
            if (correoObj != null && !correoObj.toString().trim().isEmpty()) {
                correo = correoObj.toString().trim();
            } else if (venta.getCorreoCliente() != null
                    && !venta.getCorreoCliente().trim().isEmpty()) {
                correo = venta.getCorreoCliente().trim();
            }

            if (correo == null) {
                return ResponseEntity.badRequest()
                        .body("Debe proporcionar un correo válido.");
            }

            Cliente cliente = venta.getCliente();

            Map<String, Object> pdfPayload = new HashMap<>();
            pdfPayload.put("tipoComprobante",   venta.getTipoComprobante());
            pdfPayload.put("serie",             venta.getSerie());
            pdfPayload.put("numeroComprobante", venta.getNumeroComprobante());
            pdfPayload.put("documentoCliente",
                    cliente != null ? cliente.getNumeroDocumento() : "00000000");
            pdfPayload.put("nombreCliente",
                    cliente != null ? cliente.getRazonSocialNombre() : "Público General");
            pdfPayload.put("metodoPago", venta.getMetodoPago());
            pdfPayload.put("total",      venta.getTotal());

            List<Map<String, Object>> itemsList = new ArrayList<>();
            for (DetalleVenta det : venta.getDetalles()) {
                Map<String, Object> map = new HashMap<>();
                map.put("codigo",         det.getProducto().getCodigoInterno());
                map.put("nombre",         det.getProducto().getNombre());
                map.put("cantidad",       det.getCantidad());
                map.put("precioUnitario", det.getPrecioUnitario());
                map.put("importe",        det.getSubtotal());
                itemsList.add(map);
            }
            pdfPayload.put("items", itemsList);

            byte[] pdfBytes = pdfService.generarComprobantePdf(pdfPayload, false);
            byte[] xmlBytes = ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<Invoice><Data>Documento Electronico SUNAT"
                    + " Motorepuestos Tato</Data></Invoice>").getBytes();
            byte[] cdrBytes = ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<ApplicationResponse><Response>Aceptado</Response>"
                    + "</ApplicationResponse>").getBytes();

            String docNombre = venta.getSerie() + "-" + venta.getNumeroComprobante();
            String cuerpo = "<h3>¡Gracias por su compra en Motorepuestos Tato!</h3>"
                    + "<p>Adjuntamos su comprobante electrónico oficial"
                    + " (PDF, XML y constancia CDR).</p>";

            emailService.enviarCorreoConArchivos(correo,
                    "Comprobante Electrónico " + docNombre, cuerpo,
                    pdfBytes, docNombre + ".pdf",
                    xmlBytes, docNombre + ".xml",
                    cdrBytes, "CDR-" + docNombre + ".xml");

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
            Cliente c = venta.getCliente();

            Map<String, Object> pdfPayload = new HashMap<>();
            pdfPayload.put("tipoComprobante",   venta.getTipoComprobante());
            pdfPayload.put("serie",             venta.getSerie());
            pdfPayload.put("numeroComprobante", venta.getNumeroComprobante());
            pdfPayload.put("documentoCliente",  c != null ? c.getNumeroDocumento() : "00000000");
            pdfPayload.put("nombreCliente",     c != null ? c.getRazonSocialNombre() : "Público General");
            pdfPayload.put("metodoPago",        venta.getMetodoPago());
            pdfPayload.put("total",             venta.getTotal());

            List<Map<String, Object>> itemsList = new ArrayList<>();
            for (DetalleVenta det : venta.getDetalles()) {
                Map<String, Object> map = new HashMap<>();
                map.put("codigo",         det.getProducto().getCodigoInterno());
                map.put("nombre",         det.getProducto().getNombre());
                map.put("cantidad",       det.getCantidad());
                map.put("precioUnitario", det.getPrecioUnitario());
                map.put("importe",        det.getSubtotal());
                itemsList.add(map);
            }
            pdfPayload.put("items", itemsList);

            byte[] pdfBytes = pdfService.generarComprobantePdf(pdfPayload, false);

            return ResponseEntity.ok()
                    .header("Content-Type", "application/pdf")
                    .header("Content-Disposition", "inline; filename=\"" +
                            venta.getSerie() + "-" + venta.getNumeroComprobante() + ".pdf\"")
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}