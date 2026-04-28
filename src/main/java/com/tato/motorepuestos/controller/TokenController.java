package com.tato.motorepuestos.controller;

import com.tato.motorepuestos.model.TokenAcceso;
import com.tato.motorepuestos.model.Usuario;
import com.tato.motorepuestos.repository.UsuarioRepository;
import com.tato.motorepuestos.service.TokenAccesoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Map;
import java.util.Optional;

@Controller
public class TokenController {

    @Autowired private TokenAccesoService tokenAccesoService;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;


    @GetMapping("/activar-cuenta")
    public String paginaActivacion(@RequestParam String token,
                                   org.springframework.ui.Model model) {
        Optional<TokenAcceso> opt = tokenAccesoService
                .buscarVigente(token, TokenAcceso.TipoToken.ACTIVACION);
        if (opt.isEmpty()) return "redirect:/token-invalido";
        model.addAttribute("token", token);
        model.addAttribute("tipo", "activacion");
        return "forward:/establecer-password.html";
    }

    @GetMapping("/restablecer-password")
    public String paginaReset(@RequestParam String token,
                              org.springframework.ui.Model model) {
        Optional<TokenAcceso> opt = tokenAccesoService
                .buscarVigente(token, TokenAcceso.TipoToken.RESET);
        if (opt.isEmpty()) return "redirect:/token-invalido";
        return "forward:/establecer-password.html";
    }

    @PostMapping("/api/token/establecer-password")
    @ResponseBody
    public ResponseEntity<?> establecerPassword(@RequestBody Map<String, String> body) {
        String tokenStr  = body.get("token");
        String password  = body.get("password");
        String tipoStr   = body.get("tipo");

        TokenAcceso.TipoToken tipo = "activacion".equals(tipoStr)
                ? TokenAcceso.TipoToken.ACTIVACION
                : TokenAcceso.TipoToken.RESET;

        Optional<TokenAcceso> opt = tokenAccesoService.buscarVigente(tokenStr, tipo);
        if (opt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "El enlace ha expirado o ya fue utilizado."));
        }

        TokenAcceso tk = opt.get();
        Usuario usuario = tk.getUsuario();
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setActivo(true);
        usuarioRepository.save(usuario);
        tokenAccesoService.marcarUsado(tk);

        return ResponseEntity.ok(Map.of("mensaje", "Contraseña establecida correctamente."));
    }

    @GetMapping("/api/token/validar")
    @ResponseBody
    public ResponseEntity<?> validarToken(@RequestParam String token,
                                          @RequestParam String tipo) {
        TokenAcceso.TipoToken tipoEnum = "activacion".equals(tipo)
                ? TokenAcceso.TipoToken.ACTIVACION
                : TokenAcceso.TipoToken.RESET;
        boolean valido = tokenAccesoService.buscarVigente(token, tipoEnum).isPresent();
        return valido ? ResponseEntity.ok().build()
                : ResponseEntity.badRequest().build();
    }
}