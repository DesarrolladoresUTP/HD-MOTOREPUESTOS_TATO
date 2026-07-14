package com.tato.motorepuestos.controller;

import com.tato.motorepuestos.model.Usuario;
import com.tato.motorepuestos.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/perfil")
public class PerfilRestController {

    @Autowired
    private UsuarioService usuarioService;

    @PutMapping("/foto")
    public ResponseEntity<?> actualizarMiFoto(
            @RequestParam("foto") MultipartFile foto,
            HttpSession session) {

        Long actorId = (Long) session.getAttribute("usuarioId");
        if (actorId == null) return ResponseEntity.status(401).build();

        Long sucursalId = (Long) session.getAttribute("sucursalId");
        Usuario actualizado = usuarioService.actualizarFotoPerfil(actorId, foto, actorId, sucursalId);
        if (actualizado == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(actualizado);
    }
}