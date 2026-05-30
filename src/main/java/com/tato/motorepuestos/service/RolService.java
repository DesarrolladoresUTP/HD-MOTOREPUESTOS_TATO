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
        if (rol.getId() == null) {
            rol.setActivo(true);
        }
        return rolRepository.save(rol);
    }

    public Rol actualizarRol(Long id, Rol detallesNuevos) {
        Rol rolExistente = rolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        rolExistente.setNombre(detallesNuevos.getNombre());

        rolExistente.setPermisoUsuarios(detallesNuevos.isPermisoUsuarios());
        rolExistente.setPermisoRoles(detallesNuevos.isPermisoRoles());
        rolExistente.setPermisoProductos(detallesNuevos.isPermisoProductos());
        rolExistente.setPermisoCategorias(detallesNuevos.isPermisoCategorias());
        rolExistente.setPermisoSucursales(detallesNuevos.isPermisoSucursales());

        rolExistente.setPermisoClientes(detallesNuevos.isPermisoClientes());
        rolExistente.setPermisoWeb(detallesNuevos.isPermisoWeb());

        rolExistente.setPermisoStocks(detallesNuevos.isPermisoStocks());
        rolExistente.setPermisoTraslados(detallesNuevos.isPermisoTraslados());
        rolExistente.setPermisoHistorial(detallesNuevos.isPermisoHistorial());

        rolExistente.setPermisoComprasIngresar(detallesNuevos.isPermisoComprasIngresar());
        rolExistente.setPermisoComprasRegistro(detallesNuevos.isPermisoComprasRegistro());
        rolExistente.setPermisoVentasRealizar(detallesNuevos.isPermisoVentasRealizar());
        rolExistente.setPermisoVentasRegistro(detallesNuevos.isPermisoVentasRegistro());

        return rolRepository.save(rolExistente);
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