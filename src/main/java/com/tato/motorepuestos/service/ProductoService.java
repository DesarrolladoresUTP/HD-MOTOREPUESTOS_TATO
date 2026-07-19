package com.tato.motorepuestos.service;

import com.tato.motorepuestos.model.*;
import com.tato.motorepuestos.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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
    @Autowired
    private com.cloudinary.Cloudinary cloudinary;
    @Autowired
    private MovimientoInventarioRepository movimientoRepository;
    @Autowired
    private com.tato.motorepuestos.repository.UsuarioRepository usuarioRepository;

    public List<InventarioSucursal> listarPorSucursal(Long sucursalId) {
        return inventarioRepository.findBySucursalId(sucursalId);
    }

    public List<InventarioSucursal> listarBajoStock(Long sucursalId) {
        return inventarioRepository.findBajoStockBySucursalId(sucursalId);
    }

    public Producto obtenerPorId(Long id) {
        return productoRepository.findById(id).orElse(null);
    }

    @Transactional
    public InventarioSucursal guardarProducto(String codigoInterno, String nombre,
                                              String descripcion, String marca, Long categoriaId,
                                              BigDecimal precioCompra, BigDecimal precioVenta,
                                              Integer stock, Integer stockMinimo, MultipartFile imagen,
                                              List<String> varNombres, List<String> varUrls, List<MultipartFile> varImgs,
                                              List<Integer> varStocks,
                                              Long usuarioId, Long sucursalId) {

        if (productoRepository.existsByNombreAndIdNot(nombre, -1L)) {
            Producto existente = productoRepository.findByNombre(nombre).orElseThrow();
            if (inventarioRepository.existsByProductoIdAndSucursalId(existente.getId(), sucursalId)) {
                throw new RuntimeException("El producto \"" + nombre + "\" ya existe en esta sucursal");
            }

            procesarVariantes(existente, varNombres, varUrls, varImgs, varStocks);
            productoRepository.save(existente);
            return crearInventario(existente, sucursalId, calcularStockTotal(stock, varStocks), stockMinimo, precioCompra, precioVenta, usuarioId);
        }

        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        Producto producto = new Producto();
        producto.setCodigoInterno(generarSiguienteCodigo());
        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setMarca(marca);
        producto.setCategoria(categoria);

        String urlImagenGeneral = subirImagenCloudinary(imagen);
        if (urlImagenGeneral != null) producto.setImagen(urlImagenGeneral);

        procesarVariantes(producto, varNombres, varUrls, varImgs, varStocks);
        producto = productoRepository.save(producto);

        historialService.registrarAccion("Productos", "Creación",
                "Se registró el producto: " + nombre, usuarioId, sucursalId);

        return crearInventario(producto, sucursalId, calcularStockTotal(stock, varStocks), stockMinimo, precioCompra, precioVenta, usuarioId);
    }

    @Transactional
    public InventarioSucursal actualizarProducto(Long inventarioId, String codigoInterno,
                                                 String nombre, String descripcion, String marca, Long categoriaId,
                                                 BigDecimal precioCompra, BigDecimal precioVenta,
                                                 Integer stock, Integer stockMinimo, MultipartFile imagen,
                                                 List<String> varNombres, List<String> varUrls, List<MultipartFile> varImgs,
                                                 List<Integer> varStocks,
                                                 Long usuarioId, Long sucursalId) {

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

        String urlImagenGeneral = subirImagenCloudinary(imagen);
        if (urlImagenGeneral != null) producto.setImagen(urlImagenGeneral);

        procesarVariantes(producto, varNombres, varUrls, varImgs, varStocks);
        productoRepository.save(producto);

        inventario.setPrecioCompra(precioCompra != null ? precioCompra : BigDecimal.ZERO);
        inventario.setPrecioVenta(precioVenta);

        Integer stockTotalCalculado = calcularStockTotal(stock, varStocks);

        if(varStocks != null && !varStocks.isEmpty()) {
            inventario.setStock(stockTotalCalculado);
        } else if (stock != null) {
            inventario.setStock(stock);
        }

        if (stockMinimo != null) {
            inventario.setStockMinimo(stockMinimo);
        }

        InventarioSucursal actualizado = inventarioRepository.save(inventario);

        historialService.registrarAccion("Productos", "Actualización",
                "Se actualizaron los datos del producto: " + nombre, usuarioId, sucursalId);

        return actualizado;
    }

    private Integer calcularStockTotal(Integer stockGeneral, List<Integer> varStocks) {
        if (varStocks == null || varStocks.isEmpty()) {
            return stockGeneral != null ? stockGeneral : 0;
        }
        return varStocks.stream().filter(s -> s != null).mapToInt(Integer::intValue).sum();
    }

    private void procesarVariantes(Producto producto, List<String> nombres, List<String> urls, List<MultipartFile> imagenes, List<Integer> stocks) {
        producto.getVariantes().clear();

        if (nombres != null && !nombres.isEmpty()) {
            for (int i = 0; i < nombres.size(); i++) {
                String nombreVar = nombres.get(i);
                if (nombreVar == null || nombreVar.trim().isEmpty()) continue;

                ProductoVariante variante = new ProductoVariante();
                variante.setNombre(nombreVar.trim());
                variante.setProducto(producto);

                Integer stockVar = (stocks != null && stocks.size() > i && stocks.get(i) != null) ? stocks.get(i) : 0;
                variante.setStock(stockVar);

                MultipartFile imgFile = (imagenes != null && imagenes.size() > i) ? imagenes.get(i) : null;
                String oldUrl = (urls != null && urls.size() > i) ? urls.get(i) : null;

                if (imgFile != null && !imgFile.isEmpty()) {
                    variante.setImagen(subirImagenCloudinary(imgFile));
                } else if (oldUrl != null && !oldUrl.trim().isEmpty() && !oldUrl.equals("null")) {
                    variante.setImagen(oldUrl);
                }

                producto.getVariantes().add(variante);
            }
        }
    }

    private String subirImagenCloudinary(MultipartFile imagen) {
        if (imagen == null || imagen.isEmpty()) return null;
        try {
            Map<?, ?> resultado = cloudinary.uploader().upload(
                    imagen.getBytes(),
                    com.cloudinary.utils.ObjectUtils.asMap(
                            "folder", "productos_tato",
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

    public void cambiarEstado(Long inventarioId, boolean estado, Long usuarioId, Long sucursalId) {
        InventarioSucursal inventario = inventarioRepository.findById(inventarioId).orElseThrow();
        inventario.setActivo(estado);
        inventarioRepository.save(inventario);
        historialService.registrarAccion("Productos", estado ? "Reactivación" : "Ocultamiento",
                "Estado de producto: " + inventario.getProducto().getNombre(), usuarioId, sucursalId);
    }

    public void eliminarLogico(Long inventarioId, Long usuarioId, Long sucursalId) {
        cambiarEstado(inventarioId, false, usuarioId, sucursalId);
    }

    private InventarioSucursal crearInventario(Producto producto, Long sucursalId, Integer stock, Integer stockMinimo,
                                               BigDecimal precioCompra, BigDecimal precioVenta, Long usuarioId) {
        Sucursal sucursal = sucursalRepository.findById(sucursalId).orElseThrow();
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

    private String generarSiguienteCodigo() {
        List<Producto> todos = productoRepository.findAll();
        int max = todos.stream().mapToInt(p -> {
            try { return Integer.parseInt(p.getCodigoInterno()); } catch (Exception e) { return 0; }
        }).max().orElse(0);
        return String.format("%04d", max + 1);
    }

    @Transactional
    public InventarioSucursal ajustarInventario(Long inventarioId, Integer nuevoStock, Integer nuevoStockMinimo, String motivo, Long usuarioId, Long sucursalId) throws Exception {
        InventarioSucursal inv = inventarioRepository.findById(inventarioId)
                .orElseThrow(() -> new Exception("Inventario no encontrado en esta sucursal."));

        Integer stockAnterior = inv.getStock();

        inv.setStock(nuevoStock);
        inv.setStockMinimo(nuevoStockMinimo);

        String nombreUser = obtenerNombreUsuario(usuarioId);

        MovimientoInventario mov = new MovimientoInventario();
        mov.setInventario(inv);
        mov.setTipoMovimiento("Ajuste Manual");
        mov.setStockAnterior(stockAnterior);
        mov.setStockNuevo(nuevoStock);
        mov.setObservacion(motivo);
        mov.setFechaRegistro(java.time.LocalDateTime.now());
        mov.setNombreUsuario(nombreUser);
        movimientoRepository.save(mov);

        return inventarioRepository.save(inv);
    }

    @Transactional
    public InventarioSucursal ajustarInventarioVariantes(Long inventarioId, List<Map<String, Object>> variantesData, Integer nuevoStockMinimo, String motivoGeneral, Long usuarioId, Long sucursalId) throws Exception {
        InventarioSucursal inv = inventarioRepository.findById(inventarioId)
                .orElseThrow(() -> new Exception("Inventario no encontrado en esta sucursal."));

        Producto producto = inv.getProducto();
        String nombreUser = obtenerNombreUsuario(usuarioId);

        int stockTotalAcumulado = 0;

        for (Map<String, Object> varData : variantesData) {
            Long varId = Long.parseLong(varData.get("id").toString());
            Integer nuevoStockVar = Integer.parseInt(varData.get("stock").toString());
            String nombreVar = varData.get("nombre").toString();

            ProductoVariante varianteEncontrada = producto.getVariantes().stream()
                    .filter(v -> v.getId().equals(varId))
                    .findFirst()
                    .orElse(null);

            if (varianteEncontrada != null) {
                Integer stockAnteriorVar = varianteEncontrada.getStock() != null ? varianteEncontrada.getStock() : 0;

                if (!stockAnteriorVar.equals(nuevoStockVar)) {
                    varianteEncontrada.setStock(nuevoStockVar);

                    MovimientoInventario mov = new MovimientoInventario();
                    mov.setInventario(inv);
                    mov.setTipoMovimiento("Ajuste Manual");
                    mov.setStockAnterior(stockAnteriorVar);
                    mov.setStockNuevo(nuevoStockVar);
                    mov.setObservacion(motivoGeneral + " [Variante: " + nombreVar + "]");
                    mov.setFechaRegistro(java.time.LocalDateTime.now());
                    mov.setNombreUsuario(nombreUser);
                    movimientoRepository.save(mov);
                }

                stockTotalAcumulado += nuevoStockVar;
            }
        }

        productoRepository.save(producto);

        inv.setStock(stockTotalAcumulado);
        inv.setStockMinimo(nuevoStockMinimo);

        return inventarioRepository.save(inv);
    }

    private String obtenerNombreUsuario(Long usuarioId) {
        String nombreUser = "Sistema";
        if (usuarioId != null) {
            com.tato.motorepuestos.model.Usuario u = usuarioRepository.findById(usuarioId).orElse(null);
            if(u != null) nombreUser = u.getNombres() + " " + u.getApellidos();
        }
        return nombreUser;
    }

    public List<MovimientoInventario> obtenerHistorialInventario(Long inventarioId) {
        return movimientoRepository.findByInventarioIdOrderByFechaRegistroDesc(inventarioId);
    }
}