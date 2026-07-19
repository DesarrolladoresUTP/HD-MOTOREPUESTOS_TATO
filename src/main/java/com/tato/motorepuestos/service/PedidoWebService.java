package com.tato.motorepuestos.service;

import com.tato.motorepuestos.dto.PedidoWebDTO;
import com.tato.motorepuestos.model.DetallePedidoWeb;
import com.tato.motorepuestos.model.InventarioSucursal;
import com.tato.motorepuestos.model.PedidoWeb;
import com.tato.motorepuestos.model.UsuarioCliente;
import com.tato.motorepuestos.model.MovimientoInventario;
import com.tato.motorepuestos.repository.InventarioSucursalRepository;
import com.tato.motorepuestos.repository.PedidoWebRepository;
import com.tato.motorepuestos.repository.SucursalRepository;
import com.tato.motorepuestos.repository.UsuarioClienteRepository;
import com.tato.motorepuestos.repository.MovimientoInventarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.tato.motorepuestos.model.Sucursal;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private MovimientoInventarioRepository movimientoRepository;

    @Autowired
    private com.cloudinary.Cloudinary cloudinary;

    @Transactional
    public PedidoWeb procesarPedido(PedidoWebDTO dto, Long clienteWebId, MultipartFile comprobante) throws Exception {
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

        // MODIFICADO: El estado inicial ahora es Confirmando Pago
        pedido.setEstado("Confirmando Pago");

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

        // Subir comprobante a Cloudinary
        if (comprobante != null && !comprobante.isEmpty()) {
            String urlComprobante = subirImagenCloudinary(comprobante);
            if (urlComprobante != null) {
                pedido.setUrlComprobante(urlComprobante);
            }
        } else {
            throw new Exception("El comprobante de pago es obligatorio.");
        }

        BigDecimal total = BigDecimal.ZERO;

        for (PedidoWebDTO.ItemCarritoDTO item : dto.getCarrito()) {
            InventarioSucursal inv = inventarioRepository.findByProductoIdAndSucursalId(item.getId(), 1L)
                    .orElseThrow(() -> new Exception("El producto '" + item.getNombre() + "' ya no existe en el catálogo."));

            if (inv.getStock() < item.getCantidad()) {
                throw new Exception("¡Lo sentimos! El producto '" + item.getNombre() + "' se acaba de agotar o no tiene stock suficiente. Quedan: " + inv.getStock() + " unidades.");
            }

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

            Integer stockAnterior = inv.getStock();

            inv.setStock(stockAnterior - item.getCantidad());
            int vendidas = inv.getUnidadesVendidas() == null ? 0 : inv.getUnidadesVendidas();
            inv.setUnidadesVendidas(vendidas + item.getCantidad());
            inventarioRepository.save(inv);

            MovimientoInventario mov = new MovimientoInventario();
            mov.setInventario(inv);
            mov.setTipoMovimiento("Venta Web");
            mov.setStockAnterior(stockAnterior);
            mov.setStockNuevo(inv.getStock());
            mov.setObservacion("Pedido de " + pedido.getNombreCompleto() + " | Vía: " + pedido.getMetodoEntrega());
            mov.setFechaRegistro(LocalDateTime.now());
            mov.setNombreUsuario("Cliente Web: " + pedido.getNombreCompleto());
            movimientoRepository.save(mov);
        }

        pedido.setTotal(total);
        return pedidoRepository.save(pedido);
    }

    private String subirImagenCloudinary(MultipartFile imagen) {
        if (imagen == null || imagen.isEmpty()) return null;
        try {
            Map<?, ?> resultado = cloudinary.uploader().upload(
                    imagen.getBytes(),
                    com.cloudinary.utils.ObjectUtils.asMap(
                            "folder", "comprobantes_tato",
                            "overwrite", true,
                            "resource_type", "image"
                    )
            );
            return resultado.get("secure_url").toString();
        } catch (Exception e) {
            System.err.println("Error subiendo imagen a Cloudinary: " + e.getMessage());
            return null;
        }
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