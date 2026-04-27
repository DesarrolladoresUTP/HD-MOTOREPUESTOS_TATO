package com.tato.motorepuestos.controller;

import com.tato.motorepuestos.model.Rol;
import com.tato.motorepuestos.model.Usuario;
import com.tato.motorepuestos.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioRestController {

    @Autowired
    private UsuarioService usuarioService;

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
}