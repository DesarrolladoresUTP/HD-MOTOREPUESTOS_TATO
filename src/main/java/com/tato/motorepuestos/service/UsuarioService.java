package com.tato.motorepuestos.service;

import com.tato.motorepuestos.model.TokenAcceso;
import com.tato.motorepuestos.model.Usuario;
import com.tato.motorepuestos.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private HistorialService historialService;

    @Autowired
    private TokenAccesoService tokenAccesoService;

    @Autowired
    private EmailService emailService;

    private final String UPLOAD_DIR = "uploads/";

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario obtenerUsuarioPorId(Long id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    public Usuario guardarUsuario(Usuario usuario, MultipartFile foto, Long actorId, Long sucursalId, String baseUrl) {
        usuario.setActivo(false);
        usuario.setPassword("");
        Usuario guardado = guardarConFoto(usuario, foto);

        String token = tokenAccesoService.generarToken(guardado, TokenAcceso.TipoToken.ACTIVACION);
        String enlace = baseUrl + "/activar-cuenta?token=" + token;

        try {
            String cuerpo = """
            <p>Hola <strong>%s</strong>,</p>
            <p>Tu cuenta en <strong>Motorepuestos Tato</strong> ha sido creada.</p>
            <p>Para activarla y establecer tu contraseña, haz clic en el siguiente enlace
            (válido por 24 horas):</p>
            <p><a href="%s" style="background:#0ea5e9;color:white;padding:10px 20px;
            border-radius:6px;text-decoration:none;">Activar mi cuenta</a></p>
            <p>Si no solicitaste esto, ignora este correo.</p>
            """.formatted(guardado.getNombres(), enlace);

            emailService.enviarCorreoConPdf(
                    guardado.getCorreoElectronico(),
                    "Activa tu cuenta - Motorepuestos Tato",
                    cuerpo, null, null
            );
        } catch (Exception ignored) {}

        historialService.registrarAccion("Usuarios", "Creación",
                "Se registró al usuario: " + guardado.getNombres() + " " + guardado.getApellidos(),
                actorId, sucursalId);
        return guardado;
    }

    public Usuario actualizarUsuario(Long id, Usuario detallesNuevos, MultipartFile foto, Long actorId, Long sucursalId) {
        Usuario usuarioExistente = obtenerUsuarioPorId(id);

        if (usuarioExistente != null) {
            usuarioExistente.setNombres(detallesNuevos.getNombres());
            usuarioExistente.setApellidos(detallesNuevos.getApellidos());
            usuarioExistente.setCorreoElectronico(detallesNuevos.getCorreoElectronico());
            usuarioExistente.setRol(detallesNuevos.getRol());

            Usuario actualizado = guardarConFoto(usuarioExistente, foto);
            historialService.registrarAccion("Usuarios", "Actualización", "Se editaron los datos del usuario: " + actualizado.getNombres(), actorId, sucursalId);
            return actualizado;
        }
        return null;
    }

    public void cambiarEstadoUsuario(Long id, Long actorId, Long sucursalId) {
        Usuario usuario = obtenerUsuarioPorId(id);
        if (usuario != null) {
            usuario.setActivo(false);
            usuarioRepository.save(usuario);
            historialService.registrarAccion("Usuarios", "Desactivación", "Se suspendió el acceso al usuario: " + usuario.getNombres(), actorId, sucursalId);
        }
    }

    public void activarUsuario(Long id, Long actorId, Long sucursalId) {
        Usuario usuario = obtenerUsuarioPorId(id);
        if (usuario != null) {
            usuario.setActivo(true);
            usuarioRepository.save(usuario);
            historialService.registrarAccion("Usuarios", "Reactivación", "Se restauró el acceso al usuario: " + usuario.getNombres(), actorId, sucursalId);
        }
    }

    public void eliminarLogico(Long id, Long actorId, Long sucursalId) {
        Usuario usuario = obtenerUsuarioPorId(id);
        if (usuario != null) {
            usuario.setActivo(false);
            usuarioRepository.save(usuario);
            historialService.registrarAccion("Usuarios", "Eliminación Lógica",
                    "Se desactivó definitivamente al usuario: "
                            + usuario.getNombres() + " " + usuario.getApellidos(),
                    actorId, sucursalId);
        }
    }

    private Usuario guardarConFoto(Usuario usuario, MultipartFile foto) {
        if (foto != null && !foto.isEmpty()) {
            try {
                Path directorio = Paths.get(UPLOAD_DIR);
                if (!Files.exists(directorio)) Files.createDirectories(directorio);

                String nombreFoto = System.currentTimeMillis() + "_" + foto.getOriginalFilename();
                Path rutaArchivo = directorio.resolve(nombreFoto);
                Files.copy(foto.getInputStream(), rutaArchivo);
                usuario.setFoto(nombreFoto);
            } catch (Exception e) {}
        }
        return usuarioRepository.save(usuario);
    }

    public void enviarResetPassword(Long id, Long actorId, Long sucursalId, String baseUrl) {
        Usuario usuario = obtenerUsuarioPorId(id);
        if (usuario == null) return;

        String token = tokenAccesoService.generarToken(usuario, TokenAcceso.TipoToken.RESET);
        String enlace = baseUrl + "/restablecer-password?token=" + token;

        try {
            String cuerpo = """
            <p>Hola <strong>%s</strong>,</p>
            <p>Se solicitó el restablecimiento de tu contraseña en <strong>Motorepuestos Tato</strong>.</p>
            <p>Haz clic en el enlace para definir una nueva contraseña
            (válido por 24 horas):</p>
            <p><a href="%s" style="background:#0369a1;color:white;padding:10px 20px;
            border-radius:6px;text-decoration:none;">Restablecer contraseña</a></p>
            <p>Si no solicitaste esto, ignora este correo.</p>
            """.formatted(usuario.getNombres(), enlace);

            emailService.enviarCorreoConPdf(
                    usuario.getCorreoElectronico(),
                    "Restablecer contraseña - Motorepuestos Tato",
                    cuerpo, null, null
            );
        } catch (Exception ignored) {}

        historialService.registrarAccion("Usuarios", "Reset Contraseña",
                "Se envió correo de restablecimiento a: " + usuario.getNombres(),
                actorId, sucursalId);
    }
}