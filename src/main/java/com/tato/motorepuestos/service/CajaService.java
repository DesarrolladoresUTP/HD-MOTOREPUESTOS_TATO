package com.tato.motorepuestos.service;

import com.tato.motorepuestos.model.Caja;
import com.tato.motorepuestos.repository.CajaRepository;
import com.tato.motorepuestos.repository.SucursalRepository;
import com.tato.motorepuestos.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class CajaService {

    @Autowired private CajaRepository cajaRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private SucursalRepository sucursalRepository;

    public Caja obtenerCajaActiva(Long usuarioId, Long sucursalId) {
        return cajaRepository.findTopByUsuarioIdAndSucursalIdAndEstadoOrderByIdDesc(
                usuarioId, sucursalId, "ABIERTA").orElse(null);
    }

    @Transactional
    public Caja abrirCaja(BigDecimal montoInicial, Long usuarioId, Long sucursalId) throws Exception {
        if (obtenerCajaActiva(usuarioId, sucursalId) != null) {
            throw new Exception("Ya tienes una caja abierta. Ciérrala antes de abrir una nueva.");
        }
        Caja caja = new Caja();
        caja.setFechaApertura(LocalDateTime.now());
        caja.setMontoInicial(montoInicial);
        caja.setEstado("ABIERTA");
        caja.setUsuario(usuarioRepository.findById(usuarioId).orElseThrow());
        caja.setSucursal(sucursalRepository.findById(sucursalId).orElseThrow());
        return cajaRepository.save(caja);
    }

    @Transactional
    public Caja cerrarCaja(BigDecimal montoReal, String observaciones, Long usuarioId, Long sucursalId) throws Exception {
        Caja caja = cajaRepository.findTopByUsuarioIdAndSucursalIdAndEstadoOrderByIdDesc(
                        usuarioId, sucursalId, "ABIERTA")
                .orElseThrow(() -> new Exception("No tienes ninguna caja abierta."));

        BigDecimal ventasEfectivo = cajaRepository.sumVentasEfectivoDesde(
                usuarioId, sucursalId, caja.getFechaApertura());

        BigDecimal montoEsperado = caja.getMontoInicial().add(ventasEfectivo);

        caja.setMontoEsperado(montoEsperado);
        caja.setMontoReal(montoReal);
        caja.setDiferencia(montoReal.subtract(montoEsperado));
        caja.setObservaciones(observaciones);
        caja.setFechaCierre(LocalDateTime.now());
        caja.setEstado("CERRADA");

        return cajaRepository.save(caja);
    }

    public Map<String, Object> obtenerResumenCaja(Long usuarioId, Long sucursalId) {
        Caja caja = obtenerCajaActiva(usuarioId, sucursalId);
        if (caja == null) return Map.of("estado", "CERRADA");

        BigDecimal ventasEfectivo = cajaRepository.sumVentasEfectivoDesde(
                usuarioId, sucursalId, caja.getFechaApertura());
        BigDecimal montoEsperado = caja.getMontoInicial().add(ventasEfectivo);

        return Map.of(
                "estado", "ABIERTA",
                "id", caja.getId(),
                "montoInicial", caja.getMontoInicial(),
                "ventasEfectivo", ventasEfectivo,
                "montoEsperado", montoEsperado,
                "fechaApertura", caja.getFechaApertura().toString()
        );
    }
}