package com.tato.motorepuestos.service;

import com.tato.motorepuestos.model.*;
import com.tato.motorepuestos.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Orquesta todo lo que necesita el panel de "Configuracion Almacen
 * Complementario" y el flujo de venta con espera de almacenero.
 *
 * - Config del grid y ubicaciones de productos: se escriben DIRECTO en la
 *   BD (son las mismas tablas que usa el backend complementario, no hace
 *   falta llamarlo para esto).
 * - Generar perfil IA y notificar pedidos: SI requieren llamar al backend
 *   complementario (ahi vive Gemini/Cloudinary/Firebase).
 */
@Service
public class AlmacenComplementarioService {

    @Autowired private ConfigAlmacenRepository configAlmacenRepository;
    @Autowired private UbicacionProductoRepository ubicacionProductoRepository;
    @Autowired private PedidoAlmacenRepository pedidoAlmacenRepository;
    @Autowired private DetallePedidoAlmacenRepository detallePedidoAlmacenRepository;
    @Autowired private SucursalRepository sucursalRepository;
    @Autowired private ProductoRepository productoRepository;
    @Autowired private InventarioSucursalRepository inventarioSucursalRepository;

    @Value("${almacen.backend.url}")
    private String almacenBackendUrl;

    @Value("${almacen.backend.internal-key}")
    private String internalApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final String UPLOAD_DIR = "uploads/";

    // ==================== Config del grid + toggle ====================

    public ConfigAlmacen obtenerOCrearConfig(Long sucursalId) {
        return configAlmacenRepository.findBySucursalId(sucursalId).orElseGet(() -> {
            ConfigAlmacen nueva = new ConfigAlmacen();
            nueva.setSucursal(sucursalRepository.findById(sucursalId).orElseThrow());
            nueva.setTotalEstantes(5);
            nueva.setTotalFilas(3);
            nueva.setTotalColumnas(5);
            nueva.setRequiereConfirmacionAlmacenero(false);
            return configAlmacenRepository.save(nueva);
        });
    }

    public ConfigAlmacen guardarConfig(Long sucursalId, Integer totalEstantes, Integer totalFilas,
                                       Integer totalColumnas, Boolean requiereConfirmacion) {
        ConfigAlmacen config = obtenerOCrearConfig(sucursalId);
        config.setTotalEstantes(totalEstantes);
        config.setTotalFilas(totalFilas);
        config.setTotalColumnas(totalColumnas);
        config.setRequiereConfirmacionAlmacenero(requiereConfirmacion);
        return configAlmacenRepository.save(config);
    }

    public boolean requiereConfirmacionAlmacenero(Long sucursalId) {
        return configAlmacenRepository.findBySucursalId(sucursalId)
                .map(ConfigAlmacen::getRequiereConfirmacionAlmacenero)
                .orElse(false);
    }

    // ==================== Ubicaciones (escritura directa) ====================

    public List<Map<String, Object>> getProductosSinUbicacion(Long sucursalId) {
        String url = almacenBackendUrl + "/api/admin/productos-sin-ubicacion?sucursalId=" + sucursalId;
        HttpEntity<Void> entity = new HttpEntity<>(construirHeadersInternos(0L, sucursalId));
        return restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, entity,
                new org.springframework.core.ParameterizedTypeReference<List<Map<String, Object>>>() {}).getBody();
    }

    public UbicacionProducto asignarUbicacion(Long productoId, Long sucursalId,
                                              Integer estante, Integer fila, Integer columna) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        Sucursal sucursal = sucursalRepository.findById(sucursalId)
                .orElseThrow(() -> new RuntimeException("Sucursal no encontrada"));

        UbicacionProducto ubicacion = ubicacionProductoRepository
                .findByProductoIdAndSucursalId(productoId, sucursalId)
                .orElseGet(UbicacionProducto::new);

        ubicacion.setProducto(producto);
        ubicacion.setSucursal(sucursal);
        ubicacion.setEstante(estante);
        ubicacion.setFila(fila);
        ubicacion.setColumna(columna);

        return ubicacionProductoRepository.save(ubicacion);
    }

    // ==================== Perfil IA (proxy al backend complementario) ====================

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getProductosSinPerfilIa() {
        String url = almacenBackendUrl + "/api/admin/productos-sin-perfil-ia";
        HttpEntity<Void> entity = new HttpEntity<>(construirHeadersInternos(0L, 0L));
        return restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, entity,
                new org.springframework.core.ParameterizedTypeReference<List<Map<String, Object>>>() {}).getBody();
    }

    /**
     * Reenvia la foto que el producto YA tiene guardada en uploads/ (la que
     * se subio al crearlo) hacia el backend complementario para que Gemini
     * genere el perfil IA. No hace falta que el admin suba una foto nueva.
     */
    public Map<String, Object> generarPerfilIa(Long productoId) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        if (producto.getImagen() == null || producto.getImagen().isBlank()) {
            throw new RuntimeException("Este producto no tiene una foto guardada. Sube una foto en Productos primero.");
        }

        byte[] bytes;
        try {
            Path ruta = Paths.get(UPLOAD_DIR).resolve(producto.getImagen());
            bytes = Files.readAllBytes(ruta);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo leer la foto del producto: " + e.getMessage());
        }

        String url = almacenBackendUrl + "/api/admin/productos/" + productoId + "/generar-perfil-ia";

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("fotos", new ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return producto.getImagen();
            }
        });

        HttpHeaders headers = construirHeadersInternos(0L, 0L);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        return restTemplate.postForObject(url, entity, Map.class);
    }

    // ==================== Flujo de venta con espera de almacenero ====================

    /**
     * Crea el pedido de almacen (PENDIENTE) para una venta ya guardada, y
     * dispara la notificacion push. Se llama SOLO si
     * requiereConfirmacionAlmacenero(sucursalId) es true. Si la notificacion
     * push falla (ej. el backend complementario esta caido), NO se revierte
     * la venta -- el pedido simplemente queda pendiente y el almacenero lo
     * vera igual la proxima vez que abra el modulo Almacen en la app.
     */
    public PedidoAlmacen crearPedidoAlmacenParaVenta(Venta venta) {
        PedidoAlmacen pedido = new PedidoAlmacen();
        pedido.setVenta(venta);
        pedido.setSucursal(venta.getSucursal());
        pedido.setEstado("PENDIENTE");
        pedido.setClienteNombre(venta.getCliente() != null ? venta.getCliente().getRazonSocialNombre() : "Cliente");
        pedido.setObservaciones("Generado automaticamente desde venta " + venta.getSerie() + "-" + venta.getNumeroComprobante());
        pedido.setFechaCreacion(LocalDateTime.now());
        pedido = pedidoAlmacenRepository.save(pedido);

        for (DetalleVenta detalleVenta : venta.getDetalles()) {
            String ubicacionSnapshot = ubicacionProductoRepository
                    .findByProductoIdAndSucursalId(detalleVenta.getProducto().getId(), venta.getSucursal().getId())
                    .map(u -> "E" + u.getEstante() + " - F" + u.getFila() + " - C" + u.getColumna())
                    .orElse("Sin ubicación asignada");

            DetallePedidoAlmacen detallePedido = new DetallePedidoAlmacen();
            detallePedido.setPedidoAlmacen(pedido);
            detallePedido.setProducto(detalleVenta.getProducto());
            detallePedido.setCantidad(detalleVenta.getCantidad());
            detallePedido.setUbicacionSnapshot(ubicacionSnapshot);
            detallePedido.setRecogido(false);
            detallePedidoAlmacenRepository.save(detallePedido);
        }

        try {
            String url = almacenBackendUrl + "/api/almacen/pedidos/" + pedido.getId() + "/notificar";
            HttpEntity<Void> entity = new HttpEntity<>(construirHeadersInternos(0L, venta.getSucursal().getId()));
            restTemplate.postForEntity(url, entity, Void.class);
        } catch (Exception e) {
            // No revertimos la venta ni el pedido si la notificacion push falla.
            System.err.println("No se pudo notificar al almacenero (el pedido igual quedo creado): " + e.getMessage());
        }

        return pedido;
    }

    /** Usado por el POS para hacer polling mientras espera al almacenero. */
    public String getEstadoPedidoPorVenta(Long ventaId) {
        return pedidoAlmacenRepository.findByVentaId(ventaId)
                .map(PedidoAlmacen::getEstado)
                .orElse(null);
    }

    // ==================== Helper ====================

    private HttpHeaders construirHeadersInternos(Long usuarioId, Long sucursalId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Internal-Api-Key", internalApiKey);
        headers.set("X-Usuario-Id", String.valueOf(usuarioId));
        headers.set("X-Sucursal-Id", String.valueOf(sucursalId));
        return headers;
    }
}