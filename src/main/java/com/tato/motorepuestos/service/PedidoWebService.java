package com.tato.motorepuestos.service;

import com.tato.motorepuestos.dto.PedidoWebDTO;
import com.tato.motorepuestos.model.DetallePedidoWeb;
import com.tato.motorepuestos.model.InventarioSucursal;
import com.tato.motorepuestos.model.PedidoWeb;
import com.tato.motorepuestos.model.UsuarioCliente;
import com.tato.motorepuestos.repository.InventarioSucursalRepository;
import com.tato.motorepuestos.repository.PedidoWebRepository;
import com.tato.motorepuestos.repository.SucursalRepository;
import com.tato.motorepuestos.repository.UsuarioClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.tato.motorepuestos.model.Sucursal;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PedidoWebService {

    @Autowired
    private PedidoWebRepository pedidoRepository;

    @Autowired
    private UsuarioClienteRepository usuarioClienteRepository;

    @Autowired
    private InventarioSucursalRepository inventarioRepository;

    @Autowired
    private SucursalRepository sucursalRepository;

    @Transactional
    public PedidoWeb procesarPedido(PedidoWebDTO dto, Long clienteWebId) throws Exception {
        if (dto.getCarrito() == null || dto.getCarrito().isEmpty()) {
            throw new Exception("El carrito está vacío.");
        }

        PedidoWeb pedido = new PedidoWeb();
        pedido.setTipoDocumento(dto.getTipoDocumento());
        pedido.setNumeroDocumento(dto.getNumeroDocumento());
        pedido.setNombreCompleto(dto.getNombreCompleto());
        pedido.setTelefono(dto.getTelefono());
        pedido.setMetodoEntrega(dto.getMetodoEntrega());
        pedido.setDireccionEntrega(dto.getDireccionEntrega());
        pedido.setEstado("PENDIENTE");

        if (dto.getSucursalId() != null) {
            sucursalRepository.findById(dto.getSucursalId()).ifPresent(suc -> {
                pedido.setSucursalRetiro(suc);
                pedido.setSucursalNombre(suc.getNombre());
            });
        }

        if (clienteWebId != null) {
            UsuarioCliente cliente = usuarioClienteRepository.findById(clienteWebId).orElse(null);
            pedido.setUsuarioCliente(cliente);
        }

        BigDecimal total = BigDecimal.ZERO;

        for (PedidoWebDTO.ItemCarritoDTO item : dto.getCarrito()) {
            DetallePedidoWeb detalle = new DetallePedidoWeb();
            detalle.setProductoId(item.getId());
            detalle.setNombreProducto(item.getNombre());
            detalle.setCantidad(item.getCantidad());

            BigDecimal precio = BigDecimal.valueOf(item.getPrecio());
            BigDecimal subtotal = precio.multiply(BigDecimal.valueOf(item.getCantidad()));

            detalle.setPrecioUnitario(precio);
            detalle.setSubtotal(subtotal);

            pedido.addDetalle(detalle);
            total = total.add(subtotal);

            Long sucId = dto.getSucursalId() != null ? dto.getSucursalId() : 1L;
            InventarioSucursal inv = inventarioRepository.findByProductoIdAndSucursalId(item.getId(), sucId)
                    .orElse(null);
            if (inv != null) {
                inv.setStock(inv.getStock() - item.getCantidad());
                int vendidas = inv.getUnidadesVendidas() == null ? 0 : inv.getUnidadesVendidas();
                inv.setUnidadesVendidas(vendidas + item.getCantidad());
                inventarioRepository.save(inv);
            }
        }

        pedido.setTotal(total);
        return pedidoRepository.save(pedido);
    }

    public List<PedidoWeb> listarTodos() {
        return pedidoRepository.findAllByOrderByFechaPedidoDesc();
    }

    public List<PedidoWeb> listarMisPedidos(Long clienteWebId) {
        return pedidoRepository.findByUsuarioClienteIdOrderByFechaPedidoDesc(clienteWebId);
    }

    public void actualizarEstado(Long pedidoId, String nuevoEstado) throws Exception {
        PedidoWeb pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new Exception("Pedido no encontrado."));
        pedido.setEstado(nuevoEstado);
        pedidoRepository.save(pedido);
    }

    public PedidoWeb obtenerPorId(Long id) {
        return pedidoRepository.findById(id).orElse(null);
    }
}