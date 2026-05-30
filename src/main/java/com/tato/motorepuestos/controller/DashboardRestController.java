package com.tato.motorepuestos.controller;

import com.tato.motorepuestos.model.InventarioSucursal;
import com.tato.motorepuestos.model.PedidoWeb;
import com.tato.motorepuestos.service.PedidoWebService;
import com.tato.motorepuestos.service.ProductoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardRestController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private PedidoWebService pedidoWebService;

    @GetMapping("/resumen")
    public ResponseEntity<?> obtenerResumen(HttpSession session) {
        Long sucursalId = (Long) session.getAttribute("sucursalId");
        if (sucursalId == null) sucursalId = 1L;

        Map<String, Object> data = new HashMap<>();

        try {
            List<InventarioSucursal> inventario = productoService.listarPorSucursal(sucursalId);
            List<InventarioSucursal> bajoStock = productoService.listarBajoStock(sucursalId);
            List<PedidoWeb> pedidos = pedidoWebService.listarTodos();

            long pedidosPendientes = pedidos.stream().filter(p -> p.getEstado().equals("PENDIENTE")).count();
            double ingresosWeb = pedidos.stream()
                    .filter(p -> p.getEstado().equals("ENTREGADO") || p.getEstado().equals("ENVIADO") || p.getEstado().equals("LISTO_RECOJO"))
                    .mapToDouble(p -> p.getTotal().doubleValue())
                    .sum();

            data.put("totalProductos", inventario.size());
            data.put("bajoStockCount", bajoStock.size());
            data.put("pedidosPendientes", pedidosPendientes);
            data.put("ingresosWeb", ingresosWeb);

            Map<String, Integer> topVendidos = inventario.stream()
                    .filter(i -> i.getUnidadesVendidas() != null && i.getUnidadesVendidas() > 0)
                    .sorted((a, b) -> b.getUnidadesVendidas().compareTo(a.getUnidadesVendidas()))
                    .limit(5)
                    .collect(Collectors.toMap(
                            i -> i.getProducto().getNombre(),
                            InventarioSucursal::getUnidadesVendidas,
                            (e1, e2) -> e1,
                            java.util.LinkedHashMap::new
                    ));
            data.put("graficoTopVendidos", topVendidos);

            Map<String, Long> pedidosEstado = pedidos.stream()
                    .collect(Collectors.groupingBy(PedidoWeb::getEstado, Collectors.counting()));
            data.put("graficoPedidos", pedidosEstado);

            List<Map<String, Object>> topBajoStock = bajoStock.stream()
                    .limit(5)
                    .map(inv -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("nombre", inv.getProducto().getNombre());
                        map.put("stock", inv.getStock());
                        map.put("minimo", inv.getStockMinimo());
                        return map;
                    })
                    .collect(Collectors.toList());
            data.put("tablaBajoStock", topBajoStock);

            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al cargar dashboard: " + e.getMessage());
        }
    }
}