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
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

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
            String url = apiUrlBase.trim() + endpoint + numero.trim();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiToken.trim());
            headers.set("Accept", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            return ResponseEntity.ok(response.getBody());
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"error\": \"Error interno en el servidor local\"}");
        }
    }
}