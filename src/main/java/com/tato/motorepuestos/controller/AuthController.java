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

            HttpSession session = request.getSession(true);
            session.setAttribute("usuarioId",  usuario.getId());

            try {
                session.setAttribute("sucursalId", Long.parseLong(sucursalId));
            } catch (NumberFormatException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Sucursal inválida"));
            }

            Rol rol = usuario.getRol();
            boolean esAdmin = rol != null && "Administrador".equalsIgnoreCase(rol.getNombre());

            session.setAttribute("p_usuarios",         esAdmin || (rol != null && rol.isPermisoUsuarios()));
            session.setAttribute("p_roles",            esAdmin || (rol != null && rol.isPermisoRoles()));
            session.setAttribute("p_productos",        esAdmin || (rol != null && rol.isPermisoProductos()));
            session.setAttribute("p_categorias",       esAdmin || (rol != null && rol.isPermisoCategorias()));
            session.setAttribute("p_sucursales",       esAdmin || (rol != null && rol.isPermisoSucursales()));
            session.setAttribute("p_stocks",           esAdmin || (rol != null && rol.isPermisoStocks()));
            session.setAttribute("p_traslados",        esAdmin || (rol != null && rol.isPermisoTraslados()));
            session.setAttribute("p_historial",        esAdmin || (rol != null && rol.isPermisoHistorial()));
            session.setAttribute("p_compras_ingresar", esAdmin || (rol != null && rol.isPermisoComprasIngresar()));
            session.setAttribute("p_compras_registro", esAdmin || (rol != null && rol.isPermisoComprasRegistro()));
            session.setAttribute("p_ventas_realizar",  esAdmin || (rol != null && rol.isPermisoVentasRealizar()));
            session.setAttribute("p_ventas_registro",  esAdmin || (rol != null && rol.isPermisoVentasRegistro()));

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
                boolean esAdmin = rol != null && "Administrador".equalsIgnoreCase(rol.getNombre());

                Map<String, Boolean> permisos = new HashMap<>();
                permisos.put("permisoUsuarios",        esAdmin || (rol != null && rol.isPermisoUsuarios()));
                permisos.put("permisoRoles",           esAdmin || (rol != null && rol.isPermisoRoles()));
                permisos.put("permisoProductos",       esAdmin || (rol != null && rol.isPermisoProductos()));
                permisos.put("permisoCategorias",      esAdmin || (rol != null && rol.isPermisoCategorias()));
                permisos.put("permisoSucursales",      esAdmin || (rol != null && rol.isPermisoSucursales()));
                permisos.put("permisoStocks",          esAdmin || (rol != null && rol.isPermisoStocks()));
                permisos.put("permisoTraslados",       esAdmin || (rol != null && rol.isPermisoTraslados()));
                permisos.put("permisoHistorial",       esAdmin || (rol != null && rol.isPermisoHistorial()));
                permisos.put("permisoComprasIngresar", esAdmin || (rol != null && rol.isPermisoComprasIngresar()));
                permisos.put("permisoComprasRegistro", esAdmin || (rol != null && rol.isPermisoComprasRegistro()));
                permisos.put("permisoVentasRealizar",  esAdmin || (rol != null && rol.isPermisoVentasRealizar()));
                permisos.put("permisoVentasRegistro",  esAdmin || (rol != null && rol.isPermisoVentasRegistro()));
                Map<String, Object> userData = new HashMap<>();
                userData.put("id",           usuario.getId());
                userData.put("nombre",       usuario.getNombres() + " " + usuario.getApellidos());
                userData.put("foto",         usuario.getFoto());
                userData.put("permisos",     permisos);
                userData.put("sucursalActiva", session.getAttribute("sucursalId"));

                return ResponseEntity.ok(userData);
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}