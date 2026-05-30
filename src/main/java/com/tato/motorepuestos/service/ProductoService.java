package com.tato.motorepuestos.service;

import com.tato.motorepuestos.model.Categoria;
import com.tato.motorepuestos.model.InventarioSucursal;
import com.tato.motorepuestos.model.Producto;
import com.tato.motorepuestos.model.Sucursal;
import com.tato.motorepuestos.repository.CategoriaRepository;
import com.tato.motorepuestos.repository.InventarioSucursalRepository;
import com.tato.motorepuestos.repository.ProductoRepository;
import com.tato.motorepuestos.repository.SucursalRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private InventarioSucursalRepository inventarioRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private SucursalRepository sucursalRepository;

    @Autowired
    private HistorialService historialService;

    private final String UPLOAD_DIR = "uploads/";

    public List<InventarioSucursal> listarPorSucursal(Long sucursalId) {
        return inventarioRepository.findBySucursalId(sucursalId);
    }

    public List<InventarioSucursal> listarBajoStock(Long sucursalId) {
        return inventarioRepository.findBajoStockBySucursalId(sucursalId);
    }

    public Producto obtenerPorId(Long id) {
        return productoRepository.findById(id).orElse(null);
    }

    public InventarioSucursal obtenerInventarioPorProductoYSucursal(Long productoId, Long sucursalId) {
        return inventarioRepository.findByProductoIdAndSucursalId(productoId, sucursalId)
                .orElse(null);
    }

    @Transactional
    public InventarioSucursal guardarProducto(String codigoInterno, String nombre,
                                              String descripcion, String marca, Long categoriaId,
                                              BigDecimal precioCompra, BigDecimal precioVenta,
                                              Integer stock, Integer stockMinimo,
                                              MultipartFile imagen, Long usuarioId, Long sucursalId) {


        if (productoRepository.existsByNombreAndIdNot(nombre, -1L)) {
            Producto existente = productoRepository.findByNombre(nombre).orElseThrow();
            if (inventarioRepository.existsByProductoIdAndSucursalId(existente.getId(), sucursalId)) {
                throw new RuntimeException("El producto \"" + nombre + "\" ya existe en esta sucursal");
            }
            return crearInventario(existente, sucursalId, stock, stockMinimo,
                    precioCompra, precioVenta, usuarioId);
        }
        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        if (!Boolean.TRUE.equals(categoria.getActivo())) {
            throw new RuntimeException("La categoría seleccionada está inactiva");
        }

        String codigoGenerado = generarSiguienteCodigo();
        Producto producto = new Producto();
        producto.setCodigoInterno(codigoGenerado);
        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setMarca(marca);
        producto.setCategoria(categoria);
        producto = guardarConImagen(producto, imagen);

        historialService.registrarAccion("Productos", "Creación",
                "Se registró el producto: " + nombre + " (" + codigoGenerado + ")",
                usuarioId, sucursalId);

        return crearInventario(producto, sucursalId, stock, stockMinimo,
                precioCompra, precioVenta, usuarioId);
    }

    @Transactional
    public InventarioSucursal actualizarProducto(Long inventarioId, String codigoInterno,
                                                 String nombre, String descripcion, String marca, Long categoriaId,
                                                 BigDecimal precioCompra, BigDecimal precioVenta,
                                                 Integer stock, Integer stockMinimo,
                                                 MultipartFile imagen, Long usuarioId, Long sucursalId) {

        InventarioSucursal inventario = inventarioRepository.findById(inventarioId)
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado"));

        Producto producto = inventario.getProducto();

        if (productoRepository.existsByNombreAndIdNot(nombre, producto.getId())) {
            throw new RuntimeException("Ya existe un producto con el nombre: " + nombre);
        }

        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setMarca(marca);
        producto.setCategoria(categoria);
        guardarConImagen(producto, imagen);

        inventario.setPrecioCompra(precioCompra != null ? precioCompra : BigDecimal.ZERO);
        inventario.setPrecioVenta(precioVenta);
        inventario.setStock(stock != null ? stock : 0);
        inventario.setStockMinimo(stockMinimo != null ? stockMinimo : 5);
        InventarioSucursal actualizado = inventarioRepository.save(inventario);

        historialService.registrarAccion("Productos", "Actualización",
                "Se actualizaron los datos del producto: " + nombre,
                usuarioId, sucursalId);

        return actualizado;
    }

    public void cambiarEstado(Long inventarioId, boolean estado, Long usuarioId, Long sucursalId) {
        InventarioSucursal inventario = inventarioRepository.findById(inventarioId)
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado"));
        inventario.setActivo(estado);
        inventarioRepository.save(inventario);

        String accion = estado ? "Reactivación" : "Ocultamiento";
        historialService.registrarAccion("Productos", accion,
                "Se cambió el estado del producto: " + inventario.getProducto().getNombre(),
                usuarioId, sucursalId);
    }

    public void eliminarLogico(Long inventarioId, Long usuarioId, Long sucursalId) {
        cambiarEstado(inventarioId, false, usuarioId, sucursalId);
    }

    private InventarioSucursal crearInventario(Producto producto, Long sucursalId,
                                               Integer stock, Integer stockMinimo,
                                               BigDecimal precioCompra, BigDecimal precioVenta,
                                               Long usuarioId) {

        Sucursal sucursal = sucursalRepository.findById(sucursalId)
                .orElseThrow(() -> new RuntimeException("Sucursal no encontrada"));

        InventarioSucursal inventario = new InventarioSucursal();
        inventario.setProducto(producto);
        inventario.setSucursal(sucursal);
        inventario.setStock(stock != null ? stock : 0);
        inventario.setStockMinimo(stockMinimo != null ? stockMinimo : 5);
        inventario.setPrecioCompra(precioCompra != null ? precioCompra : BigDecimal.ZERO);
        inventario.setPrecioVenta(precioVenta != null ? precioVenta : BigDecimal.ZERO);
        inventario.setActivo(true);

        return inventarioRepository.save(inventario);
    }

    private Producto guardarConImagen(Producto producto, MultipartFile imagen) {
        if (imagen != null && !imagen.isEmpty()) {
            try {
                Path directorio = Paths.get(UPLOAD_DIR);
                if (!Files.exists(directorio)) Files.createDirectories(directorio);
                String nombreImagen = System.currentTimeMillis() + "_" + imagen.getOriginalFilename();
                Files.copy(imagen.getInputStream(), directorio.resolve(nombreImagen));
                producto.setImagen(nombreImagen);
            } catch (Exception e) {
                System.err.println("Error guardando imagen: " + e.getMessage());
            }
        }
        return productoRepository.save(producto);
    }

    private String generarSiguienteCodigo() {
        List<Producto> todos = productoRepository.findAll();
        int max = todos.stream()
                .mapToInt(p -> {
                    try { return Integer.parseInt(p.getCodigoInterno()); }
                    catch (Exception e) { return 0; }
                })
                .max()
                .orElse(0);
        return String.format("%04d", max + 1);
    }

    @Transactional
    public InventarioSucursal ajustarInventario(Long inventarioId, Integer nuevoStock, Integer nuevoStockMinimo, Long usuarioId, Long sucursalId) throws Exception {
        InventarioSucursal inv = inventarioRepository.findById(inventarioId)
                .orElseThrow(() -> new Exception("Inventario no encontrado en esta sucursal."));

        inv.setStock(nuevoStock);
        inv.setStockMinimo(nuevoStockMinimo);

        historialService.registrarAccion("Almacén", "Ajuste de Stock",
                "Se ajustó el stock del producto: " + inv.getProducto().getNombre() + " a " + nuevoStock + " unidades.",
                usuarioId, sucursalId);

        return inventarioRepository.save(inv);
    }
}