package com.tato.motorepuestos.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/consultas")
public class ConsultaController {

    @Value("${miapi.token}")
    private String apiToken;

    @Value("${miapi.url.base}")
    private String apiUrlBase;

    @GetMapping("/documento/{numero}")
    public ResponseEntity<?> consultarDocumento(@PathVariable String numero) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String endpoint = numero.length() == 8 ? "dni/" : "ruc/";
            String url = apiUrlBase.trim() + endpoint + numero.trim() + "?token=" + apiToken.trim();

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", "Bearer " + apiToken.trim());
            headers.set("Accept", "application/json");
            org.springframework.http.HttpEntity<Void> entity = new org.springframework.http.HttpEntity<>(headers);

            org.springframework.http.ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, Map.class);

            Map<String, Object> resultado = response.getBody();
            System.out.println("[ConsultaController] Respuesta: " + resultado);

            Map<String, String> respuesta = new HashMap<>();
            String nombreDetectado = null;

            if (resultado != null) {
                if (resultado.containsKey("nombre_completo"))
                    nombreDetectado = (String) resultado.get("nombre_completo");
                else if (resultado.containsKey("nombre"))
                    nombreDetectado = (String) resultado.get("nombre");
                else if (resultado.containsKey("razon_social"))
                    nombreDetectado = (String) resultado.get("razon_social");
                else if (resultado.containsKey("razonSocial"))
                    nombreDetectado = (String) resultado.get("razonSocial");
                else if (resultado.containsKey("datos") && resultado.get("datos") instanceof Map) {
                    Map<String, Object> datos = (Map<String, Object>) resultado.get("datos");
                    if (datos.containsKey("nombre_completo")) nombreDetectado = (String) datos.get("nombre_completo");
                    else if (datos.containsKey("nombre")) nombreDetectado = (String) datos.get("nombre");
                    else if (datos.containsKey("razon_social")) nombreDetectado = (String) datos.get("razon_social");
                    else if (datos.containsKey("nombres")) {
                        String nom = (String) datos.get("nombres");
                        String apP = datos.containsKey("ape_paterno") ? (String) datos.get("ape_paterno") : (String) datos.getOrDefault("apellidoPaterno", "");
                        String apM = datos.containsKey("ape_materno") ? (String) datos.get("ape_materno") : (String) datos.getOrDefault("apellidoMaterno", "");
                        nombreDetectado = (nom + " " + apP + " " + apM).trim();
                    }
                }
                else if (resultado.containsKey("data") && resultado.get("data") instanceof Map) {
                    Map<String, Object> data = (Map<String, Object>) resultado.get("data");
                    if (data.containsKey("nombre_completo")) nombreDetectado = (String) data.get("nombre_completo");
                    else if (data.containsKey("nombre")) nombreDetectado = (String) data.get("nombre");
                    else if (data.containsKey("razon_social")) nombreDetectado = (String) data.get("razon_social");
                }
            }

            if (nombreDetectado != null && !nombreDetectado.trim().isEmpty()) {
                respuesta.put("nombre", nombreDetectado.trim().toUpperCase());
                return ResponseEntity.ok(respuesta);
            }

            return ResponseEntity.status(404).body("No encontrado");

        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            System.out.println("Error HTTP: " + e.getStatusCode() + " | " + e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            System.out.println("Error general: " + e.getMessage());
            return ResponseEntity.status(500).body("Error interno: " + e.getMessage());
        }
    }
}