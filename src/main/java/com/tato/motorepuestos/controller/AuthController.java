package com.tato.motorepuestos.controller;

import com.tato.motorepuestos.model.Rol;
import com.tato.motorepuestos.model.Usuario;
import com.tato.motorepuestos.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credenciales,
                                   HttpServletRequest request) {
        String correo    = credenciales.get("correo");
        String password  = credenciales.get("password");
        String sucursalId = credenciales.get("local");

        Usuario usuario = usuarioRepository.findByCorreoElectronico(correo);

        if (usuario != null
                && Boolean.TRUE.equals(usuario.getActivo())
                && passwordEncoder.matches(password, usuario.getPassword())) {

            Rol rol = usuario.getRol();
            boolean esAdmin = rol != null
                    && "Administrador".equalsIgnoreCase(rol.getNombre());

            HttpSession session = request.getSession();
            session.setAttribute("usuarioId",  usuario.getId());
            session.setAttribute("sucursalId", Long.parseLong(sucursalId));

            session.setAttribute("p_usuarios",         esAdmin || Boolean.TRUE.equals(rol.getPermisoUsuarios()));
            session.setAttribute("p_roles",            esAdmin || Boolean.TRUE.equals(rol.getPermisoRoles()));
            session.setAttribute("p_productos",        esAdmin || Boolean.TRUE.equals(rol.getPermisoProductos()));
            session.setAttribute("p_categorias",       esAdmin || Boolean.TRUE.equals(rol.getPermisoCategorias()));
            session.setAttribute("p_sucursales",       esAdmin || Boolean.TRUE.equals(rol.getPermisoSucursales()));
            session.setAttribute("p_stocks",           esAdmin || Boolean.TRUE.equals(rol.getPermisoStocks()));
            session.setAttribute("p_traslados",        esAdmin || Boolean.TRUE.equals(rol.getPermisoTraslados()));
            session.setAttribute("p_historial",        esAdmin || Boolean.TRUE.equals(rol.getPermisoHistorial()));
            session.setAttribute("p_compras_ingresar", esAdmin || Boolean.TRUE.equals(rol.getPermisoComprasIngresar()));
            session.setAttribute("p_compras_registro", esAdmin || Boolean.TRUE.equals(rol.getPermisoComprasRegistro()));
            session.setAttribute("p_ventas_realizar",  esAdmin || Boolean.TRUE.equals(rol.getPermisoVentasRealizar()));
            session.setAttribute("p_ventas_registro",  esAdmin || Boolean.TRUE.equals(rol.getPermisoVentasRegistro()));

            return ResponseEntity.ok(Map.of("mensaje", "Login exitoso"));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Credenciales incorrectas"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        return ResponseEntity.ok(Map.of("mensaje", "Logout exitoso"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session != null && session.getAttribute("usuarioId") != null) {
            Long userId = (Long) session.getAttribute("usuarioId");
            Usuario usuario = usuarioRepository.findById(userId).orElse(null);

            if (usuario != null) {
                Rol rol = usuario.getRol();
                boolean esAdmin = rol != null
                        && "Administrador".equalsIgnoreCase(rol.getNombre());

                Map<String, Boolean> permisos = new HashMap<>();
                permisos.put("permisoUsuarios",        esAdmin || Boolean.TRUE.equals(rol.getPermisoUsuarios()));
                permisos.put("permisoRoles",           esAdmin || Boolean.TRUE.equals(rol.getPermisoRoles()));
                permisos.put("permisoProductos",       esAdmin || Boolean.TRUE.equals(rol.getPermisoProductos()));
                permisos.put("permisoCategorias",      esAdmin || Boolean.TRUE.equals(rol.getPermisoCategorias()));
                permisos.put("permisoSucursales",      esAdmin || Boolean.TRUE.equals(rol.getPermisoSucursales()));
                permisos.put("permisoStocks",          esAdmin || Boolean.TRUE.equals(rol.getPermisoStocks()));
                permisos.put("permisoTraslados",       esAdmin || Boolean.TRUE.equals(rol.getPermisoTraslados()));
                permisos.put("permisoHistorial",       esAdmin || Boolean.TRUE.equals(rol.getPermisoHistorial()));
                permisos.put("permisoComprasIngresar", esAdmin || Boolean.TRUE.equals(rol.getPermisoComprasIngresar()));
                permisos.put("permisoComprasRegistro", esAdmin || Boolean.TRUE.equals(rol.getPermisoComprasRegistro()));
                permisos.put("permisoVentasRealizar",  esAdmin || Boolean.TRUE.equals(rol.getPermisoVentasRealizar()));
                permisos.put("permisoVentasRegistro",  esAdmin || Boolean.TRUE.equals(rol.getPermisoVentasRegistro()));

                Map<String, Object> userData = new HashMap<>();
                userData.put("nombre",       usuario.getNombres());
                userData.put("foto",         usuario.getFoto());
                userData.put("permisos",     permisos);
                userData.put("sucursalActiva", session.getAttribute("sucursalId"));

                return ResponseEntity.ok(userData);
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}