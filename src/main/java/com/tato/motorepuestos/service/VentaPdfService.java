package com.tato.motorepuestos.service;

import com.tato.motorepuestos.model.Venta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

@Service
public class VentaPdfService {

    @Autowired
    private TemplateEngine templateEngine;

    public byte[] generarPdfVenta(Venta venta) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Context context = new Context();
            Map<String, Object> data = new HashMap<>();

            // Valores por defecto para Público General
            String nombreCliente = "PÚBLICO EN GENERAL";
            String documento = "00000000";
            String tipoDoc = "DNI";

            if (venta.getCliente() != null) {
                nombreCliente = venta.getCliente().getRazonSocialNombre() != null ?
                        venta.getCliente().getRazonSocialNombre() : "PÚBLICO EN GENERAL";
                documento = venta.getCliente().getNumeroDocumento() != null ?
                        venta.getCliente().getNumeroDocumento() : "00000000";
                tipoDoc = (documento.length() == 11) ? "RUC" : "DNI";
            }

            // Inyectamos las variables al HTML
            data.put("venta", venta);
            data.put("clienteNombre", nombreCliente);
            data.put("clienteDoc", documento);
            data.put("tipoDoc", tipoDoc);

            context.setVariables(data);

            // Renderizamos el HTML
            String htmlContent = templateEngine.process("pdf_venta", context);

            // Convertimos el HTML a PDF usando Flying Saucer
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(htmlContent);
            renderer.layout();
            renderer.createPDF(outputStream);

            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el PDF de la venta: " + e.getMessage(), e);
        }
    }
}