package com.tato.motorepuestos.service;

import com.tato.motorepuestos.model.Historial;
import com.tato.motorepuestos.model.Sucursal;
import com.tato.motorepuestos.model.Usuario;
import com.tato.motorepuestos.repository.HistorialRepository;
import com.tato.motorepuestos.repository.SucursalRepository;
import com.tato.motorepuestos.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class HistorialService {

    @Autowired
    private HistorialRepository historialRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SucursalRepository sucursalRepository;

    public void registrarAccion(String modulo, String accion, String descripcion,
                                Long usuarioId, Long sucursalId) {
        Historial log = new Historial();
        log.setModulo(modulo);
        log.setAccion(accion);
        log.setDescripcion(descripcion);
        log.setFecha(LocalDateTime.now());

        usuarioRepository.findById(usuarioId).ifPresent(log::setUsuario);
        sucursalRepository.findById(sucursalId).ifPresent(log::setSucursal);

        historialRepository.save(log);
    }

    public List<Historial> listarTodo() {
        return historialRepository.findAllByOrderByFechaDesc();
    }

    public List<Historial> listarPorSucursal(Long sucursalId) {
        return historialRepository.findBySucursalIdOrderByFechaDesc(sucursalId);
    }
}