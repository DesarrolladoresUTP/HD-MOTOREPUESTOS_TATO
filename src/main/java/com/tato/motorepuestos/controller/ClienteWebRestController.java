package com.tato.motorepuestos.controller;

import com.tato.motorepuestos.model.UsuarioCliente;
import com.tato.motorepuestos.service.UsuarioClienteService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/clientes-web")
public class ClienteWebRestController {

    @Autowired
    private UsuarioClienteService service;

    @PostMapping("/registro")
    public ResponseEntity<?> registrar(@RequestBody UsuarioCliente cliente) {
        try {
            UsuarioCliente nuevoCliente = service.registrar(cliente);
            return ResponseEntity.ok(nuevoCliente);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credenciales, HttpSession session) {
        try {
            UsuarioCliente cliente = service.login(credenciales.get("email"), credenciales.get("password"));

            session.setAttribute("clienteWebId", cliente.getId());
            session.setAttribute("clienteWebNombre", cliente.getNombreCompleto());

            cliente.setPassword(null);
            return ResponseEntity.ok(cliente);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/recuperar-password")
    public ResponseEntity<?> recuperar(@RequestParam String email) {
        try {
            service.enviarCorreoRecuperacion(email);
            return ResponseEntity.ok("Correo enviado con éxito.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.removeAttribute("clienteWebId");
        session.removeAttribute("clienteWebNombre");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/restablecer-password")
    public ResponseEntity<?> restablecerPassword(@RequestParam String token, @RequestBody Map<String, String> datos) {
        try {
            String nuevoPassword = datos.get("password");
            if (nuevoPassword == null || nuevoPassword.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("La contraseña no puede estar vacía.");
            }

            service.restablecerPassword(token, nuevoPassword);
            return ResponseEntity.ok("Contraseña actualizada con éxito.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}