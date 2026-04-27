package com.tato.motorepuestos.service;

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


    private final String UPLOAD_DIR = "uploads/";

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario obtenerUsuarioPorId(Long id) {
        return usuarioRepository.findById(id).orElse(null);
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

}