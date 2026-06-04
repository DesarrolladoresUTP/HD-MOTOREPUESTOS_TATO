package com.tato.motorepuestos.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private SeguridadInterceptor seguridadInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String rutaUploads = Paths.get("uploads").toAbsolutePath().toUri().toString();

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(rutaUploads);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(seguridadInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/",
                        "/error",
                        "/uploads/**",
                        "/login",
                        "/login.html",
                        "/api/auth/login",
                        "/api/sucursales/activas",
                        "/api/sucursales/con-stock",          // ? AÑADIR
                        "/activar-cuenta",
                        "/restablecer-password",
                        "/token-invalido",
                        "/establecer-password.html",
                        "/api/token/**",
                        "/tienda",
                        "/tienda.html",
                        "/tienda.js",
                        "/api/productos/activos",
                        "/checkout",
                        "/checkout.html",
                        "/checkout.js",
                        "/api/clientes/buscar-api",
                        "/api/pedidos-web/procesar",
                        "/api/pedidos-web/mis-pedidos",
                        "/api/pedidos-web/*/boleta-pdf",      // ? AÑADIR
                        "/api/consultas/documento/*",         // ? AÑADIR
                        "/login-cliente",
                        "/login-cliente.html",
                        "/restablecer-cliente",
                        "/restablecer-cliente.html",
                        "/api/clientes-web/**",
                        "/mis-pedidos",
                        "/mis-pedidos.html",
                        "/navbar.js",
                        "/navbar.js*"                         // ? AÑADIR si no está
                );
    }
}