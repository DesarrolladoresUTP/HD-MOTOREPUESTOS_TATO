package com.tato.motorepuestos.service;

import com.tato.motorepuestos.model.Sucursal;
import com.tato.motorepuestos.repository.SucursalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SucursalService {

    @Autowired
    private SucursalRepository sucursalRepository;

    public List<Sucursal> listarTodas() {
        return sucursalRepository.findAll();
    }

    public List<Sucursal> listarActivas() {
        return sucursalRepository.findByActivoTrue();
    }

    public Sucursal obtenerPorId(Long id) {
        return sucursalRepository.findById(id).orElse(null);
    }

}