package com.tato.motorepuestos.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ConsultaApiService {

    @Value("${miapi.token}")
    private String apiToken;

    @Value("${miapi.url.base}")
    private String baseUrl;

    public String consultarDocumento(String tipo, String numero) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiToken);
        headers.set("Accept", "application/json");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        String endpoint = tipo.equalsIgnoreCase("DNI") ? "/dni/" : "/ruc/";

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + endpoint + numero,
                    HttpMethod.GET,
                    entity,
                    String.class
            );
            return response.getBody();
        } catch (Exception e) {
            System.err.println("Error en API: " + e.getMessage());
            return null;
        }
    }
}