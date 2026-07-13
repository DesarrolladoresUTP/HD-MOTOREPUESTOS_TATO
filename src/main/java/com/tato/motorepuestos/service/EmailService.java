package com.tato.motorepuestos.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name}")
    private String senderName;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void enviarCorreoConArchivos(String destinatario, String asunto, String cuerpo,
                                        byte[] pdfBytes, String nombrePdf,
                                        byte[] xmlBytes, String nombreXml,
                                        byte[] cdrBytes, String nombreCdr) throws Exception {

        List<Map<String, String>> adjuntos = new ArrayList<>();
        agregarAdjunto(adjuntos, pdfBytes, nombrePdf);
        agregarAdjunto(adjuntos, xmlBytes, nombreXml);
        agregarAdjunto(adjuntos, cdrBytes, nombreCdr);

        enviar(destinatario, asunto, cuerpo, adjuntos);
    }

    public void enviarCorreoConPdf(String destinatario, String asunto, String cuerpo,
                                   byte[] pdfBytes, String nombrePdf) throws Exception {
        enviarCorreoConArchivos(destinatario, asunto, cuerpo, pdfBytes, nombrePdf, null, null, null, null);
    }

    public void enviarCorreoSimple(String destinatario, String asunto, String cuerpo) throws Exception {
        enviar(destinatario, asunto, cuerpo, null);
    }

    private void agregarAdjunto(List<Map<String, String>> adjuntos, byte[] contenido, String nombre) {
        if (contenido != null && nombre != null) {
            adjuntos.add(Map.of(
                    "content", Base64.getEncoder().encodeToString(contenido),
                    "name", nombre
            ));
        }
    }

    private void enviar(String destinatario, String asunto, String htmlContent,
                        List<Map<String, String>> adjuntos) throws Exception {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("sender", Map.of("name", senderName, "email", senderEmail));
            body.put("to", List.of(Map.of("email", destinatario)));
            body.put("subject", asunto);
            body.put("htmlContent", htmlContent);
            if (adjuntos != null && !adjuntos.isEmpty()) {
                body.put("attachment", adjuntos);
            }

            String json = objectMapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                    .header("accept", "application/json")
                    .header("api-key", brevoApiKey)
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 300) {
                throw new RuntimeException("Error enviando correo: " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("No se pudo enviar el correo a " + destinatario, e);
        }
    }
}