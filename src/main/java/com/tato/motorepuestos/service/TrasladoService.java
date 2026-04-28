package com.tato.motorepuestos.service;

import com.tato.motorepuestos.model.InventarioSucursal;
import com.tato.motorepuestos.model.Producto;
import com.tato.motorepuestos.model.Sucursal;
import com.tato.motorepuestos.repository.InventarioSucursalRepository;
import com.tato.motorepuestos.repository.ProductoRepository;
import com.tato.motorepuestos.repository.SucursalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public void procesarTraslado(Long sucursalOrigenId, Long sucursalDestinoId,
                                 List<Map<String, Object>> items, Long usuarioId) {

        if (sucursalOrigenId.equals(sucursalDestinoId)) {
            throw new RuntimeException("La sucursal de origen y destino no pueden ser la misma");
        }

        Sucursal destino = sucursalRepository.findById(sucursalDestinoId)
                .orElseThrow(() -> new RuntimeException("Sucursal destino no encontrada"));

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

            origen.setStock(origen.getStock() - cantidad);
            inventarioRepository.save(origen);

            InventarioSucursal destinoInv = inventarioRepository
                    .findByProductoIdAndSucursalId(origen.getProducto().getId(), sucursalDestinoId)
                    .orElseGet(() -> {
                        InventarioSucursal nuevo = new InventarioSucursal();
                        nuevo.setProducto(origen.getProducto());
                        nuevo.setSucursal(destino);
                        nuevo.setStock(0);
                        nuevo.setStockMinimo(origen.getStockMinimo());
                        nuevo.setPrecioCompra(origen.getPrecioCompra());
                        nuevo.setPrecioVenta(origen.getPrecioVenta());
                        nuevo.setActivo(true);
                        return nuevo;
                    });

            destinoInv.setStock(destinoInv.getStock() + cantidad);
            inventarioRepository.save(destinoInv);

            historialService.registrarAccion(
                    "Traslados",
                    "Envío de Mercadería",
                    "Se trasladaron " + cantidad + " unid. del producto '"
                            + origen.getProducto().getNombre() + "' hacia " + destino.getNombre(),
                    usuarioId,
                    sucursalOrigenId
            );
        }
    }
}