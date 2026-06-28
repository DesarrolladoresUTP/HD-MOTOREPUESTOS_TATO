package com.tato.motorepuestos.service;
import com.tato.motorepuestos.dto.CotizacionDTO;
import com.tato.motorepuestos.model.Cotizacion;
import com.tato.motorepuestos.model.DetalleCotizacion;
import com.tato.motorepuestos.repository.CotizacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@Service
public class CotizacionService {
    @Autowired
    private CotizacionRepository cotizacionRepository;
    @Transactional
    public Cotizacion generarCotizacion(CotizacionDTO dto) throws Exception {
        if (dto.getCarrito() == null || dto.getCarrito().isEmpty()) {
            throw new Exception("El carrito está vacío.");
        }
        Cotizacion cotizacion = new Cotizacion();
        cotizacion.setNombreCliente(dto.getNombreCliente());
        cotizacion.setDocumentoCliente(dto.getDocumentoCliente());
        cotizacion.setCorreoCliente(dto.getCorreoCliente());
        cotizacion.setCodigo("TEMP-" + System.currentTimeMillis());
        BigDecimal total = BigDecimal.ZERO;
        for (CotizacionDTO.ItemDTO item : dto.getCarrito()) {
            DetalleCotizacion detalle = new DetalleCotizacion();
            detalle.setProductoId(item.getId());
            detalle.setNombreProducto(item.getNombre());
            detalle.setCantidad(item.getCantidad());
            BigDecimal precio = BigDecimal.valueOf(item.getPrecio());
            BigDecimal subtotal = precio.multiply(BigDecimal.valueOf(item.getCantidad()));
            detalle.setPrecioUnitario(precio);
            detalle.setSubtotal(subtotal);
            cotizacion.addDetalle(detalle);
            total = total.add(subtotal);
        }
        cotizacion.setTotal(total);
        Cotizacion guardada = cotizacionRepository.save(cotizacion);
        guardada.setCodigo(String.format("COT-%05d", guardada.getId()));
        return cotizacionRepository.save(guardada);
    }
    public Cotizacion buscarYValidarCotizacion(String codigo) throws Exception {
        Cotizacion cotizacion = cotizacionRepository.findByCodigo(codigo.toUpperCase())
                .orElseThrow(() -> new Exception("Cotización no encontrada."));
        if (cotizacion.getEstado().equals("CONVERTIDA")) {
            throw new Exception("Esta cotización ya fue convertida en una venta.");
        }
        if (LocalDateTime.now().isAfter(cotizacion.getFechaVencimiento())) {
            cotizacion.setEstado("VENCIDA");
            cotizacionRepository.save(cotizacion);
            throw new Exception("La cotización ha expirado (Superó los 7 días).");
        }
        return cotizacion;
    }

    public Cotizacion buscarCotizacionParaEnvio(String codigo) throws Exception {
        return cotizacionRepository.findByCodigo(codigo.toUpperCase())
                .orElseThrow(() -> new Exception("Cotización no encontrada."));
    }
    public void marcarComoConvertida(String codigo) {
        cotizacionRepository.findByCodigo(codigo).ifPresent(c -> {
            c.setEstado("CONVERTIDA");
            cotizacionRepository.save(c);
        });
    }
    public List<Cotizacion> listarTodas() {
        return cotizacionRepository.findAllByOrderByFechaEmisionDesc();
    }
}