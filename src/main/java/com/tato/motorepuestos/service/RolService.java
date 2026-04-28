package com.tato.motorepuestos.service;

import com.tato.motorepuestos.model.Rol;
import com.tato.motorepuestos.repository.RolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RolService {

    @Autowired
    private RolRepository rolRepository;

    public List<Rol> listarTodos() {
        return rolRepository.findAll();
    }

    public List<Rol> listarActivos() {
        return rolRepository.findByActivoTrue();
    }

    public Rol obtenerPorId(Long id) {
        return rolRepository.findById(id).orElse(null);
    }

    public Rol guardarRol(Rol rol) {
        return rolRepository.save(rol);
    }

    public void cambiarEstado(Long id, boolean estado) {
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
        rol.setActivo(estado);
        rolRepository.save(rol);
    }

    public void eliminarLogico(Long id) {
        cambiarEstado(id, false);
    }
}