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
                        "/login",
                        "/login.html",
                        "/api/auth/login",
                        "/api/sucursales/activas",
                        "/uploads/**",
                        "/activar-cuenta",
                        "/restablecer-password",
                        "/token-invalido",
                        "/establecer-password.html",
                        "/api/token/**",
                        "/error"
                );
    }
}