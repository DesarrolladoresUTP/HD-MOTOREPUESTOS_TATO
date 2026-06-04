package com.tato.motorepuestos.controller;

import com.tato.motorepuestos.model.Cliente;
import com.tato.motorepuestos.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/clientes")
public class ClienteRestController {

    @Autowired
    private ClienteRepository clienteRepository;

    private final String TOKEN_MIAPICLOUD = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjozNjEsImV4cCI6MTc2MDM4Mzc4N30.K6fcsDrgk9mHqXVFDaiJK5fRt7ayS87TNxkdDmdCJpo";

    @GetMapping
    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }

    @GetMapping("/buscar")
    public ResponseEntity<?> buscarPorDocumento(@RequestParam String documento) {
        Optional<Cliente> cliente = clienteRepository.findByNumeroDocumento(documento);
        if (cliente.isPresent()) {
            return ResponseEntity.ok(cliente.get());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<?> guardarCliente(@RequestBody Cliente cliente) {
        if (cliente.getActivo() == null) cliente.setActivo(true);
        try {
            return ResponseEntity.ok(clienteRepository.save(cliente));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al guardar: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id, @RequestParam boolean activo) {
        Cliente cliente = clienteRepository.findById(id).orElseThrow();
        cliente.setActivo(activo);
        return ResponseEntity.ok(clienteRepository.save(cliente));
    }

    @GetMapping("/buscar-api")
    public ResponseEntity<?> consultarDocumentoExterno(@RequestParam String tipo, @RequestParam String numero) {
        if (TOKEN_MIAPICLOUD == null || TOKEN_MIAPICLOUD.trim().isEmpty()) {
            return ResponseEntity.status(401).body("Error: Token no configurado en el backend.");
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            Map<String, String> respuestaParaFrontend = new HashMap<>();

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", "Bearer " + TOKEN_MIAPICLOUD);
            headers.set("Accept", "application/json");
            org.springframework.http.HttpEntity<Void> entity = new org.springframework.http.HttpEntity<>(headers);

            Map<String, Object> resultado = null;

            if ("DNI".equalsIgnoreCase(tipo)) {
                String url = "https://miapi.cloud/v1/dni/" + numero + "?token=" + TOKEN_MIAPICLOUD;
                System.out.println(" Consultando DNI en la URL: " + url);

                org.springframework.http.ResponseEntity<Map> respuestaApi = restTemplate.exchange(
                        url, org.springframework.http.HttpMethod.GET, entity, Map.class
                );
                resultado = respuestaApi.getBody();

            } else if ("RUC".equalsIgnoreCase(tipo)) {
                String url = "https://miapi.cloud/v1/ruc/" + numero + "?token=" + TOKEN_MIAPICLOUD;
                System.out.println(" Consultando RUC en la URL: " + url);

                org.springframework.http.ResponseEntity<Map> respuestaApi = restTemplate.exchange(
                        url, org.springframework.http.HttpMethod.GET, entity, Map.class
                );
                resultado = respuestaApi.getBody();
            }

            System.out.println("📥 [MIAPI.CLOUD] RESPUESTA RECIBIDA: " + resultado);

            if (resultado != null) {
                String nombreDetectado = null;

                if (resultado.containsKey("nombre_completo")) {
                    nombreDetectado = (String) resultado.get("nombre_completo");
                } else if (resultado.containsKey("nombre")) {
                    nombreDetectado = (String) resultado.get("nombre");
                } else if (resultado.containsKey("razon_social")) {
                    nombreDetectado = (String) resultado.get("razon_social");
                } else if (resultado.containsKey("razonSocial")) {
                    nombreDetectado = (String) resultado.get("razonSocial");
                }

                else if (resultado.containsKey("datos") && resultado.get("datos") instanceof Map) {
                    Map<String, Object> datos = (Map<String, Object>) resultado.get("datos");

                    if (datos.containsKey("nombre_completo")) {
                        nombreDetectado = (String) datos.get("nombre_completo");
                    } else if (datos.containsKey("nombre")) {
                        nombreDetectado = (String) datos.get("nombre");
                    } else if (datos.containsKey("razon_social")) {
                        nombreDetectado = (String) datos.get("razon_social");
                    } else if (datos.containsKey("nombres")) {
                        String nom = (String) datos.get("nombres");
                        String apP = datos.containsKey("ape_paterno") ? (String) datos.get("ape_paterno") : (String) datos.get("apellidoPaterno");
                        String apM = datos.containsKey("ape_materno") ? (String) datos.get("ape_materno") : (String) datos.get("apellidoMaterno");

                        nombreDetectado = nom + (apP != null ? " " + apP : "") + (apM != null ? " " + apM : "");
                    }
                }

                else if (resultado.containsKey("data") && resultado.get("data") instanceof Map) {
                    Map<String, Object> data = (Map<String, Object>) resultado.get("data");
                    if (data.containsKey("nombre_completo")) nombreDetectado = (String) data.get("nombre_completo");
                    else if (data.containsKey("nombre")) nombreDetectado = (String) data.get("nombre");
                    else if (data.containsKey("razon_social")) nombreDetectado = (String) data.get("razon_social");
                    else if (data.containsKey("nombres")) {
                        String nom = (String) data.get("nombres");
                        String apP = data.containsKey("apellidoPaterno") ? (String) data.get("apellidoPaterno") : (String) data.get("apellido_paterno");
                        String apM = data.containsKey("apellidoMaterno") ? (String) data.get("apellidoMaterno") : (String) data.get("apellido_materno");
                        nombreDetectado = nom + (apP != null ? " " + apP : "") + (apM != null ? " " + apM : "");
                    }
                }

                if (nombreDetectado != null && !nombreDetectado.trim().isEmpty()) {
                    respuestaParaFrontend.put("nombre", nombreDetectado.trim().toUpperCase());
                    return ResponseEntity.ok(respuestaParaFrontend);
                }
            }

            return ResponseEntity.status(404).body("Estructura de nombre no encontrada en el JSON.");

        } catch (Exception e) {
            System.out.println(" Error en la consulta externa: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error interno del servidor: " + e.getMessage());
        }
    }
}