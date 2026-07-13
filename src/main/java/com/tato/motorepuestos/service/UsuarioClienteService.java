package com.tato.motorepuestos.service;

import com.tato.motorepuestos.model.UsuarioCliente;
import com.tato.motorepuestos.repository.UsuarioClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UsuarioClienteService {

    @Autowired
    private UsuarioClienteRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public UsuarioCliente registrar(UsuarioCliente cliente) throws Exception {
        if (repository.findByEmail(cliente.getEmail()).isPresent()) {
            throw new Exception("El correo ya está registrado en otra cuenta.");
        }
        if (repository.findByNumeroDocumento(cliente.getNumeroDocumento()).isPresent()) {
            throw new Exception("Este documento ya está asociado a otra cuenta.");
        }

        cliente.setPassword(passwordEncoder.encode(cliente.getPassword()));
        return repository.save(cliente);
    }

    public UsuarioCliente login(String email, String password) throws Exception {
        UsuarioCliente cliente = repository.findByEmail(email)
                .orElseThrow(() -> new Exception("Credenciales incorrectas o usuario no existe."));

        if (!passwordEncoder.matches(password, cliente.getPassword())) {
            throw new Exception("Credenciales incorrectas.");
        }
        if (!cliente.getActivo()) {
            throw new Exception("Tu cuenta ha sido suspendida. Contáctanos para más información.");
        }
        return cliente;
    }

    public void enviarCorreoRecuperacion(String email) throws Exception {
        UsuarioCliente cliente = repository.findByEmail(email)
                .orElseThrow(() -> new Exception("No existe ninguna cuenta asociada a este correo."));

        String token = UUID.randomUUID().toString();
        cliente.setTokenRecuperacion(token);
        repository.save(cliente);

        String link = baseUrl + "/restablecer-cliente.html?token=" + token;

        String asunto = "Recuperación de Contraseña - Motorepuestos Tato";
        String html = """
                <p>Hola %s,</p>
                <p>Hemos recibido una solicitud para recuperar tu contraseña en nuestra tienda virtual.</p>
                <p>Por favor, haz clic en el siguiente enlace para crear una nueva contraseña:</p>
                <p><a href="%s">%s</a></p>
                <p>Si no fuiste tú quien solicitó esto, simplemente ignora este mensaje.</p>
                <p>Atentamente,<br>El equipo de Motorepuestos Tato.</p>
                """.formatted(cliente.getNombreCompleto(), link, link);

        emailService.enviarCorreoSimple(cliente.getEmail(), asunto, html);
    }

    public void restablecerPassword(String token, String nuevoPassword) throws Exception {
        UsuarioCliente cliente = repository.findByTokenRecuperacion(token)
                .orElseThrow(() -> new Exception("El enlace de recuperación es inválido, ha expirado o ya fue utilizado."));

        cliente.setPassword(passwordEncoder.encode(nuevoPassword));
        cliente.setTokenRecuperacion(null);
        repository.save(cliente);
    }

    public UsuarioCliente obtenerPorId(Long id) {
        return repository.findById(id).orElse(null);
    }
}