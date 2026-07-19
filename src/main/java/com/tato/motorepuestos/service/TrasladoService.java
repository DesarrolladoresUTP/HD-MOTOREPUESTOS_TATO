package com.tato.motorepuestos.service;

import com.tato.motorepuestos.model.InventarioSucursal;
import com.tato.motorepuestos.model.Producto;
import com.tato.motorepuestos.model.Sucursal;
import com.tato.motorepuestos.model.Traslado;
import com.tato.motorepuestos.model.DetalleTraslado;
import com.tato.motorepuestos.model.Usuario;
import com.tato.motorepuestos.repository.InventarioSucursalRepository;
import com.tato.motorepuestos.repository.ProductoRepository;
import com.tato.motorepuestos.repository.SucursalRepository;
import com.tato.motorepuestos.repository.TrasladoRepository;
import com.tato.motorepuestos.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class TrasladoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private InventarioSucursalRepository inventarioRepository;

    @Autowired
    private SucursalRepository sucursalRepository;

    @Autowired
    private HistorialService historialService;

    // NUEVO: Repositorios necesarios para guardar el historial estructurado
    @Autowired
    private TrasladoRepository trasladoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional
    public void procesarTraslado(Long sucursalOrigenId, Long sucursalDestinoId,
                                 List<Map<String, Object>> items, Long usuarioId) {

        if (sucursalOrigenId.equals(sucursalDestinoId)) {
            throw new RuntimeException("La sucursal de origen y destino no pueden ser la misma");
        }

        Sucursal origenSuc = sucursalRepository.findById(sucursalOrigenId)
                .orElseThrow(() -> new RuntimeException("Sucursal origen no encontrada"));

        Sucursal destinoSuc = sucursalRepository.findById(sucursalDestinoId)
                .orElseThrow(() -> new RuntimeException("Sucursal destino no encontrada"));

        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);

        // NUEVO: Creamos la entidad principal del traslado para la base de datos
        Traslado traslado = new Traslado();
        traslado.setSucursalOrigen(origenSuc);
        traslado.setSucursalDestino(destinoSuc);
        traslado.setUsuario(usuario);
        traslado.setFechaRegistro(LocalDateTime.now());

        for (Map<String, Object> item : items) {
            Long inventarioOrigenId = Long.parseLong(item.get("id").toString());
            Integer cantidad = Integer.parseInt(item.get("cantidad").toString());

            if (cantidad <= 0) {
                throw new RuntimeException("La cantidad a trasladar debe ser mayor a cero");
            }

            InventarioSucursal origen = inventarioRepository.findById(inventarioOrigenId)
                    .orElseThrow(() -> new RuntimeException("Inventario origen no encontrado"));

            if (origen.getStock() < cantidad) {
                throw new RuntimeException("Stock insuficiente para: "
                        + origen.getProducto().getNombre()
                        + ". Disponible: " + origen.getStock());
            }

            // 1. Restamos en origen
            origen.setStock(origen.getStock() - cantidad);
            inventarioRepository.save(origen);

            // 2. Sumamos en destino (o creamos si no existe)
            InventarioSucursal destinoInv = inventarioRepository
                    .findByProductoIdAndSucursalId(origen.getProducto().getId(), sucursalDestinoId)
                    .orElseGet(() -> {
                        InventarioSucursal nuevo = new InventarioSucursal();
                        nuevo.setProducto(origen.getProducto());
                        nuevo.setSucursal(destinoSuc);
                        nuevo.setStock(0);
                        nuevo.setStockMinimo(origen.getStockMinimo());
                        nuevo.setPrecioCompra(origen.getPrecioCompra());
                        nuevo.setPrecioVenta(origen.getPrecioVenta());
                        nuevo.setActivo(true);
                        return nuevo;
                    });

            destinoInv.setStock(destinoInv.getStock() + cantidad);
            inventarioRepository.save(destinoInv);

            // NUEVO: Añadimos el detalle al traslado
            DetalleTraslado detalle = new DetalleTraslado();
            detalle.setProducto(origen.getProducto());
            detalle.setCantidad(cantidad);
            detalle.setTraslado(traslado);

            traslado.getDetalles().add(detalle);

            // Mantenemos tu historial simple como registro adicional
            historialService.registrarAccion(
                    "Traslados",
                    "Envío de Mercadería",
                    "Se trasladaron " + cantidad + " unid. del producto '"
                            + origen.getProducto().getNombre() + "' hacia " + destinoSuc.getNombre(),
                    usuarioId,
                    sucursalOrigenId
            );
        }

        // NUEVO: Guardamos el traslado completo con sus detalles
        trasladoRepository.save(traslado);
    }

    // NUEVO: Método que el Controlador estaba pidiendo
    public List<Traslado> listarTodos() {
        return trasladoRepository.findAllByOrderByFechaRegistroDesc();
    }
}