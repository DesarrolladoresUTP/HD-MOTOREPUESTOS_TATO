package com.tato.motorepuestos.controller;
import com.tato.motorepuestos.dto.CotizacionDTO;
import com.tato.motorepuestos.model.Cotizacion;
import com.tato.motorepuestos.model.DetalleCotizacion;
import com.tato.motorepuestos.service.CotizacionService;
import com.tato.motorepuestos.service.EmailService;
import com.tato.motorepuestos.service.PdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cotizaciones")
public class CotizacionRestController {
    @Autowired
    private CotizacionService cotizacionService;
    @Autowired
    private PdfService pdfService;
    @Autowired
    private EmailService emailService;
    @PostMapping("/generar")
    public ResponseEntity<?> generarCotizacion(@RequestBody CotizacionDTO dto) {
        try {
            Cotizacion nueva = cotizacionService.generarCotizacion(dto);
            return ResponseEntity.ok(nueva);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/buscar/{codigo}")
    public ResponseEntity<?> buscarCotizacion(@PathVariable String codigo) {
        try {
            Cotizacion cotizacion = cotizacionService.buscarYValidarCotizacion(codigo);
            return ResponseEntity.ok(cotizacion);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping
    public ResponseEntity<List<Cotizacion>> listarCotizaciones() {
        return ResponseEntity.ok(cotizacionService.listarTodas());
    }

    @PostMapping("/{codigo}/enviar")
    public ResponseEntity<?> enviarPorCorreo(@PathVariable String codigo, @RequestParam String correo) {
        try {
            Cotizacion cot = cotizacionService.buscarCotizacionParaEnvio(codigo);

            byte[] pdfBytes = pdfService.generarCotizacionPdf(cot);

            String cuerpo = "<h3>Hola " + (cot.getNombreCliente() != null ? cot.getNombreCliente() : "Cliente") + "</h3>"
                    + "<p>Adjuntamos la cotización <b>" + cot.getCodigo() + "</b> solicitada.</p>"
                    + "<p>Monto total: <b>S/ " + String.format("%.2f", cot.getTotal()) + "</b></p>"
                    + "<p>Esta cotización es válida hasta el "
                    + cot.getFechaVencimiento().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    + ".</p>";

            emailService.enviarCorreoConPdf(correo,
                    "Cotización " + cot.getCodigo() + " - Motorepuestos Tato",
                    cuerpo, pdfBytes, "Cotizacion_" + cot.getCodigo() + ".pdf");

            return ResponseEntity.ok("Cotización " + codigo + " enviada a " + correo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al enviar: " + e.getMessage());
        }
    }
}