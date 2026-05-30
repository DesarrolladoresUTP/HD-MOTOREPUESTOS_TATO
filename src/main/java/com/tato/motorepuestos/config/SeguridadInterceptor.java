package com.tato.motorepuestos.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class SeguridadInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("usuarioId") == null) {
            response.sendRedirect("/login");
            return false;
        }

        String uri = request.getRequestURI();

        if (esRutaRestringida(uri, "/usuarios", "/api/usuarios") && !tienePermiso(session, "p_usuarios")) {
            return rechazarAcceso(uri, response);
        }
        if (esRutaRestringida(uri, "/permisos", "/api/roles") && !tienePermiso(session, "p_roles")) {
            return rechazarAcceso(uri, response);
        }
        if (esRutaRestringida(uri, "/productos", "/api/productos") && !tienePermiso(session, "p_productos")) {
            return rechazarAcceso(uri, response);
        }
        if (esRutaRestringida(uri, "/categorias", "/api/categorias") && !tienePermiso(session, "p_productos")) {
            return rechazarAcceso(uri, response);
        }
        if (esRutaRestringida(uri, "/sucursales", "/api/sucursales") && !tienePermiso(session, "p_sucursales")) {
            return rechazarAcceso(uri, response);
        }
        if (esRutaRestringida(uri, "/stocks", "/api/stocks") && !tienePermiso(session, "p_stocks")) {
            return rechazarAcceso(uri, response);
        }
        if (esRutaRestringida(uri, "/traslados", "/api/traslados") && !tienePermiso(session, "p_traslados")) {
            return rechazarAcceso(uri, response);
        }
        if (uri.startsWith("/compras") && !uri.startsWith("/registro_compras") && !tienePermiso(session, "p_compras_ingresar")) {
            return rechazarAcceso(uri, response);
        }
        if (uri.startsWith("/registro_compras") && !tienePermiso(session, "p_compras_registro")) {
            return rechazarAcceso(uri, response);
        }
        if (uri.startsWith("/api/compras") && !tienePermiso(session, "p_compras_ingresar") && !tienePermiso(session, "p_compras_registro")) {
            return rechazarAcceso(uri, response);
        }
        if (uri.startsWith("/venta") && !uri.startsWith("/registro_ventas") && !tienePermiso(session, "p_ventas_realizar")) {
            return rechazarAcceso(uri, response);
        }
        if (uri.startsWith("/registro_ventas") && !tienePermiso(session, "p_ventas_registro")) {
            return rechazarAcceso(uri, response);
        }
        if (uri.startsWith("/api/ventas") && !tienePermiso(session, "p_ventas_realizar") && !tienePermiso(session, "p_ventas_registro")) {
            return rechazarAcceso(uri, response);
        }

        return true;
    }

    private boolean esRutaRestringida(String uri, String rutaWeb, String rutaApi) {
        return uri.startsWith(rutaWeb) || uri.startsWith(rutaApi);
    }

    private boolean rechazarAcceso(String uri, HttpServletResponse response) throws Exception {
        if (uri.startsWith("/api/")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        } else {
            response.sendRedirect("/inicio?error=permisos");
        }
        return false;
    }

    private boolean tienePermiso(HttpSession session, String permiso) {
        Object valor = session.getAttribute(permiso);
        return valor != null && (Boolean) valor;
    }
}