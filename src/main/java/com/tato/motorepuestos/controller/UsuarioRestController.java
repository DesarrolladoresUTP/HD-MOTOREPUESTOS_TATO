package com.tato.motorepuestos.controller;

import com.tato.motorepuestos.model.Rol;
import com.tato.motorepuestos.model.Usuario;
import com.tato.motorepuestos.repository.UsuarioRepository;
import com.tato.motorepuestos.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioRestController {

    @Autowired
    private UsuarioService usuarioService;

    // Repositorio inyectado para consultar directamente la contraseña real
    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping
    public List<Usuario> listar() {
        return usuarioService.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerUsuario(@PathVariable Long id) {
        Usuario usuario = usuarioService.obtenerUsuarioPorId(id);
        if (usuario != null) return ResponseEntity.ok(usuario);
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<?> crearUsuario(
            @RequestParam("nombres") String nombres,
            @RequestParam("apellidos") String apellidos,
            @RequestParam("correo") String correo,
            @RequestParam("rol_id") Long rolId,
            @RequestParam(value = "foto", required = false) MultipartFile foto,
            HttpServletRequest request,
            HttpSession session) {

        Usuario usuario = new Usuario();
        usuario.setNombres(nombres);
        usuario.setApellidos(apellidos);
        usuario.setCorreoElectronico(correo);
        Rol rol = new Rol(); rol.setId(rolId);
        usuario.setRol(rol);

        Long actorId    = (Long) session.getAttribute("usuarioId");
        Long sucursalId = (Long) session.getAttribute("sucursalId");
        String baseUrl  = request.getScheme() + "://" + request.getServerName()
                + (request.getServerPort() != 80 && request.getServerPort() != 443
                ? ":" + request.getServerPort() : "");

        return ResponseEntity.ok(usuarioService.guardarUsuario(usuario, foto, actorId, sucursalId, baseUrl));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarUsuario(
            @PathVariable Long id,
            @RequestParam("nombres") String nombres,
            @RequestParam("apellidos") String apellidos,
            @RequestParam("correo") String correo,
            @RequestParam("rol_id") Long rolId,
            @RequestParam(value = "foto", required = false) MultipartFile foto,
            HttpSession session) {

        Usuario detalles = new Usuario();
        detalles.setNombres(nombres);
        detalles.setApellidos(apellidos);
        detalles.setCorreoElectronico(correo);

        Rol rol = new Rol();
        rol.setId(rolId);
        detalles.setRol(rol);

        Long actorId = (Long) session.getAttribute("usuarioId");
        Long sucursalId = (Long) session.getAttribute("sucursalId");

        Usuario actualizado = usuarioService.actualizarUsuario(id, detalles, foto, actorId, sucursalId);
        return ResponseEntity.ok(actualizado);
    }

    @PutMapping("/{id}/desactivar")
    public ResponseEntity<?> desactivarUsuario(@PathVariable Long id, HttpSession session) {
        Long actorId = (Long) session.getAttribute("usuarioId");
        Long sucursalId = (Long) session.getAttribute("sucursalId");
        usuarioService.cambiarEstadoUsuario(id, actorId, sucursalId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/activar")
    public ResponseEntity<?> activarUsuario(@PathVariable Long id, HttpSession session) {
        Long actorId = (Long) session.getAttribute("usuarioId");
        Long sucursalId = (Long) session.getAttribute("sucursalId");
        usuarioService.activarUsuario(id, actorId, sucursalId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarUsuario(@PathVariable Long id, HttpSession session) {
        try {
            Long actorId    = (Long) session.getAttribute("usuarioId");
            Long sucursalId = (Long) session.getAttribute("sucursalId");
            usuarioService.eliminarLogico(id, actorId, sucursalId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/reset-password")
    public ResponseEntity<?> resetPassword(@PathVariable Long id, HttpServletRequest request, HttpSession session) {
        Long actorId    = (Long) session.getAttribute("usuarioId");
        Long sucursalId = (Long) session.getAttribute("sucursalId");
        String baseUrl  = request.getScheme() + "://" + request.getServerName()
                + (request.getServerPort() != 80 && request.getServerPort() != 443
                ? ":" + request.getServerPort() : "");
        usuarioService.enviarResetPassword(id, actorId, sucursalId, baseUrl);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verificar-credencial")
    public ResponseEntity<?> verificarCredencial(@RequestBody Map<String, String> payload, HttpSession session) {
        Long usuarioLogeadoId = (Long) session.getAttribute("usuarioId");

        if (usuarioLogeadoId == null) {
            return ResponseEntity.status(401).body("Sesión no válida o expirada.");
        }

        String passwordIngresado = payload.get("password");
        if (passwordIngresado != null) {
            passwordIngresado = passwordIngresado.trim(); // Limpiamos espacios accidentales
        }

        Usuario usuarioLogeado = usuarioRepository.findById(usuarioLogeadoId).orElse(null);

        if (usuarioLogeado != null && usuarioLogeado.getPassword() != null) {
            String passwordBd = usuarioLogeado.getPassword();

            // ESTO APARECERÁ EN TU CONSOLA DE INTELLIJ (Letras blancas)
            System.out.println("=== VALIDACIÓN DE SEGURIDAD ===");
            System.out.println("ID Usuario: " + usuarioLogeadoId);
            System.out.println("Pass ingresada: [" + passwordIngresado + "]");
            System.out.println("Pass en BD: [" + passwordBd + "]");

            boolean coinciden = false;

            // CASO A: Si la contraseña está encriptada (Spring Security BCrypt)
            if (passwordBd.startsWith("$2a$") || passwordBd.startsWith("$2b$")) {
                try {
                    org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
                    coinciden = encoder.matches(passwordIngresado, passwordBd);
                } catch (Exception e) {
                    System.out.println("Fallo al usar BCrypt: " + e.getMessage());
                }
            }
            // CASO B: Si la contraseña está en texto normal en la base de datos
            else {
                coinciden = passwordBd.equals(passwordIngresado);
            }

            System.out.println("¿Coinciden?: " + coinciden);
            System.out.println("===============================");

            if (coinciden) {
                return ResponseEntity.ok().build();
            }
        }

        return ResponseEntity.status(401).body("Contraseña incorrecta.");
    }
}