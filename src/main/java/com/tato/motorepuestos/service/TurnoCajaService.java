package com.tato.motorepuestos.service;

import com.tato.motorepuestos.model.*;
import com.tato.motorepuestos.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TurnoCajaService {

    @Autowired private TurnoCajaRepository turnoCajaRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private SucursalRepository sucursalRepository;
    @Autowired private VentaRepository ventaRepository;

    public TurnoCaja obtenerCajaActiva(Long sucursalId) {
        return turnoCajaRepository.findTopBySucursalIdAndEstadoOrderByIdDesc(sucursalId, "ABIERTA").orElse(null);
    }

    public TurnoCaja abrirCaja(BigDecimal montoInicial, Long usuarioId, Long sucursalId) throws Exception {
        if (obtenerCajaActiva(sucursalId) != null) {
            throw new Exception("Ya existe una caja abierta en esta sucursal. Ciérrela primero.");
        }

        TurnoCaja turno = new TurnoCaja();
        turno.setFechaApertura(LocalDateTime.now());
        turno.setMontoInicial(montoInicial);
        turno.setUsuario(usuarioRepository.findById(usuarioId).orElseThrow());
        turno.setSucursal(sucursalRepository.findById(sucursalId).orElseThrow());
        turno.setEstado("ABIERTA");

        return turnoCajaRepository.save(turno);
    }

    public TurnoCaja cerrarCaja(BigDecimal montoReal, String observaciones, Long sucursalId) throws Exception {
        TurnoCaja turno = turnoCajaRepository.findTopBySucursalIdAndEstadoOrderByIdDesc(sucursalId, "ABIERTA")
                .orElseThrow(() -> new Exception("No hay ninguna caja abierta para cerrar."));

        turno.setFechaCierre(LocalDateTime.now());
        turno.setEstado("CERRADA");
        turno.setMontoFinalReal(montoReal);
        turno.setObservaciones(observaciones);

        List<Venta> ventasTurno = ventaRepository.findBySucursalIdOrderByFechaDesc(sucursalId).stream()
                .filter(v -> v.getFecha().isAfter(turno.getFechaApertura()))
                .filter(v -> "Efectivo".equalsIgnoreCase(v.getMetodoPago()))
                .toList();

        BigDecimal ingresosEfectivo = ventasTurno.stream()
                .map(Venta::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        turno.setIngresosEfectivo(ingresosEfectivo);

        BigDecimal calculado = turno.getMontoInicial().add(ingresosEfectivo);
        turno.setMontoFinalCalculado(calculado);

        turno.setDiferencia(montoReal.subtract(calculado));

        return turnoCajaRepository.save(turno);
    }

    public List<TurnoCaja> listarHistorial(Long sucursalId) {
        return turnoCajaRepository.findBySucursalIdOrderByFechaAperturaDesc(sucursalId);
    }
}