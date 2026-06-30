package com.tato.motorepuestos.controller;
import com.tato.motorepuestos.model.Caja;
import com.tato.motorepuestos.model.Usuario;
import com.tato.motorepuestos.model.Venta;
import com.tato.motorepuestos.repository.CajaRepository;
import com.tato.motorepuestos.repository.SucursalRepository;
import com.tato.motorepuestos.repository.UsuarioRepository;
import com.tato.motorepuestos.repository.VentaRepository;
import com.tato.motorepuestos.service.PdfService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/cajas")
public class CajaAdminRestController {
    @Autowired private CajaRepository cajaRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private SucursalRepository sucursalRepository;
    @Autowired private VentaRepository ventaRepository;
    @Autowired private PdfService pdfService;
    // ?? GET /api/admin/cajas?usuarioId=&sucursalId=&estado=&desde=&hasta= ??
    @GetMapping
    public ResponseEntity<?> listarCajas(
            @RequestParam(required = false) Long    usuarioId,
            @RequestParam(required = false) Long    sucursalId,
            @RequestParam(required = false) String  estado,
            @RequestParam(required = false) String  desde,
            @RequestParam(required = false) String  hasta,
            HttpSession session) {
        if (!esAdmin(session)) return ResponseEntity.status(403).build();
        LocalDateTime desdeDate = desde != null && !desde.isEmpty()
                ? LocalDate.parse(desde).atStartOfDay() : null;
        LocalDateTime hastaDate = hasta != null && !hasta.isEmpty()
                ? LocalDate.parse(hasta).atTime(23, 59, 59) : null;
        List<Caja> cajas = cajaRepository.buscarConFiltros(
                usuarioId, sucursalId,
                (estado != null && !estado.isEmpty()) ? estado : null,
                desdeDate, hastaDate);
        List<Map<String, Object>> resultado = cajas.stream()
                .map(this::cajaToMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(resultado);
    }
    // ?? GET /api/admin/cajas/usuarios ? lista de usuarios que tienen cajas ??
    @GetMapping("/usuarios")
    public ResponseEntity<?> listarUsuariosConCajas(HttpSession session) {
        if (!esAdmin(session)) return ResponseEntity.status(403).build();
        List<Usuario> usuarios = usuarioRepository.findAll();
        List<Map<String, Object>> lista = usuarios.stream().map(u -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", u.getId());
            m.put("nombre", u.getNombres() + " " + u.getApellidos());
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }
    // ?? GET /api/admin/cajas/{id}/detalle ? detalle completo de UNA caja ???
    @GetMapping("/{id}/detalle")
    public ResponseEntity<?> detalleCaja(@PathVariable Long id, HttpSession session) {
        if (!esAdmin(session)) return ResponseEntity.status(403).build();
        Caja caja = cajaRepository.findById(id).orElse(null);
        if (caja == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(construirDetalleCaja(caja));
    }
    // ?? GET /api/admin/cajas/{id}/pdf ? PDF de UNA caja específica ?????????
    @GetMapping("/{id}/pdf")
    public ResponseEntity<?> pdfDetalleCaja(@PathVariable Long id, HttpSession session) {
        if (!esAdmin(session)) return ResponseEntity.status(403).build();
        try {
            Caja caja = cajaRepository.findById(id).orElse(null);
            if (caja == null) return ResponseEntity.notFound().build();
            Map<String, Object> detalle = construirDetalleCaja(caja);
            byte[] pdf = pdfService.generarDetalleCajaPdf(detalle);
            String filename = "caja_" + id + "_detalle.pdf";
            return ResponseEntity.ok()
                    .header("Content-Type", "application/pdf")
                    .header("Content-Disposition", "inline; filename=\"" + filename + "\"")
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al generar PDF: " + e.getMessage());
        }
    }
    // ?? GET /api/admin/cajas/resumen?usuarioId=&sucursalId=&desde=&hasta= ??
    @GetMapping("/resumen")
    public ResponseEntity<?> resumenPorUsuario(
            @RequestParam Long   usuarioId,
            @RequestParam(required = false) Long   sucursalId,
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta,
            HttpSession session) {
        if (!esAdmin(session)) return ResponseEntity.status(403).build();
        LocalDateTime desdeDate = desde != null && !desde.isEmpty()
                ? LocalDate.parse(desde).atStartOfDay() : null;
        LocalDateTime hastaDate = hasta != null && !hasta.isEmpty()
                ? LocalDate.parse(hasta).atTime(23, 59, 59) : null;
        List<Caja> cajas = cajaRepository.findCerradasPorUsuarioEnRango(usuarioId, sucursalId, desdeDate, hastaDate);
        BigDecimal sumInicial  = BigDecimal.ZERO;
        BigDecimal sumEsperado = BigDecimal.ZERO;
        BigDecimal sumReal     = BigDecimal.ZERO;
        BigDecimal sumDif      = BigDecimal.ZERO;
        List<Map<String, Object>> detalle = new ArrayList<>();
        for (Caja c : cajas) {
            BigDecimal ini = orZero(c.getMontoInicial());
            BigDecimal esp = orZero(c.getMontoEsperado());
            BigDecimal rea = orZero(c.getMontoReal());
            BigDecimal dif = orZero(c.getDiferencia());
            sumInicial  = sumInicial.add(ini);
            sumEsperado = sumEsperado.add(esp);
            sumReal     = sumReal.add(rea);
            sumDif      = sumDif.add(dif);
            detalle.add(cajaToMap(c));
        }
        Usuario u = usuarioRepository.findById(usuarioId).orElse(null);
        String nombreUsuario = u != null ? u.getNombres() + " " + u.getApellidos() : "?";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String rango = (desdeDate != null ? desdeDate.format(fmt) : "inicio")
                + " al " + (hastaDate != null ? hastaDate.format(fmt) : "hoy");
        String nombreSucursal = "Todas";
        if (sucursalId != null) {
            nombreSucursal = sucursalRepository.findById(sucursalId)
                    .map(com.tato.motorepuestos.model.Sucursal::getNombre)
                    .orElse("Desconocida");
        }
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("nombreUsuario", nombreUsuario);
        resp.put("rangoFechas",   rango);
        resp.put("sucursal",      nombreSucursal);
        resp.put("totalCajas",    cajas.size());
        resp.put("sumInicial",    sumInicial);
        resp.put("sumEsperado",   sumEsperado);
        resp.put("sumReal",       sumReal);
        resp.put("difTotal",      sumDif);
        resp.put("cajas",         detalle);
        return ResponseEntity.ok(resp);
    }
    // ?? GET /api/admin/cajas/resumen/pdf?usuarioId=&sucursalId=&desde=&hasta= ??
    @GetMapping("/resumen/pdf")
    public ResponseEntity<?> resumenPdf(
            @RequestParam Long   usuarioId,
            @RequestParam(required = false) Long   sucursalId,
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta,
            HttpSession session) {
        if (!esAdmin(session)) return ResponseEntity.status(403).build();
        try {
            // Reutiliza la lógica del endpoint de resumen
            ResponseEntity<?> resumenResp = resumenPorUsuario(usuarioId, sucursalId, desde, hasta, session);
            @SuppressWarnings("unchecked")
            Map<String, Object> resumen = (Map<String, Object>) resumenResp.getBody();
            byte[] pdf = pdfService.generarResumenCajasPdf(resumen);
            String filename = "resumen_caja_" + usuarioId + "_" +
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";
            return ResponseEntity.ok()
                    .header("Content-Type", "application/pdf")
                    .header("Content-Disposition", "inline; filename=\"" + filename + "\"")
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al generar PDF: " + e.getMessage());
        }
    }
    // ?? helpers ?????????????????????????????????????????????????????????????
    private boolean esAdmin(HttpSession session) {
        Object permiso = session.getAttribute("p_cajasadmin");
        return Boolean.TRUE.equals(permiso);
    }

    private Map<String, Object> construirDetalleCaja(Caja caja) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        LocalDateTime desde = caja.getFechaApertura();
        LocalDateTime hasta = caja.getFechaCierre() != null ? caja.getFechaCierre() : LocalDateTime.now();
        List<Venta> ventas = ventaRepository
                .findBySucursalAndUsuarioDesde(caja.getSucursal().getId(), caja.getUsuario().getId(), desde)
                .stream()
                .filter(v -> !v.getFecha().isAfter(hasta))
                .collect(Collectors.toList());
        BigDecimal totalVendido       = BigDecimal.ZERO;
        BigDecimal totalEfectivo      = BigDecimal.ZERO;
        BigDecimal totalAnulado       = BigDecimal.ZERO;
        int cantVentas    = 0;
        int cantAnuladas  = 0;
        List<Map<String, Object>> ventasList = new ArrayList<>();
        for (Venta v : ventas) {
            boolean anulada = "ANULADA".equalsIgnoreCase(v.getEstadoVenta());
            if (anulada) {
                cantAnuladas++;
                totalAnulado = totalAnulado.add(orZero(v.getTotal()));
            } else {
                cantVentas++;
                totalVendido = totalVendido.add(orZero(v.getTotal()));
                if ("Efectivo".equalsIgnoreCase(v.getMetodoPago())) {
                    totalEfectivo = totalEfectivo.add(orZero(v.getTotal()));
                }
            }
            Map<String, Object> vm = new LinkedHashMap<>();
            vm.put("id", v.getId());
            vm.put("numeroComprobante", v.getSerie() + "-" + v.getNumeroComprobante());
            vm.put("tipoComprobante", v.getTipoComprobante());
            vm.put("fecha", v.getFecha().format(fmt));
            vm.put("cliente", v.getCliente() != null ? v.getCliente().getRazonSocialNombre() : "Público General");
            vm.put("metodoPago", v.getMetodoPago());
            vm.put("total", v.getTotal());
            vm.put("estadoVenta", v.getEstadoVenta());
            vm.put("estadoSunat", v.getEstadoSunat());
            vm.put("productos", v.getDetalles().stream().map(d ->
                    Map.of(
                            "nombre", d.getProducto().getNombre(),
                            "codigo", d.getProducto().getCodigoInterno(),
                            "cantidad", d.getCantidad(),
                            "precioUnitario", d.getPrecioUnitario(),
                            "subtotal", d.getSubtotal()
                    )
            ).collect(Collectors.toList()));
            ventasList.add(vm);
        }
        Map<String, Object> m = new LinkedHashMap<>(cajaToMap(caja));
        m.put("cantVentas", cantVentas);
        m.put("cantAnuladas", cantAnuladas);
        m.put("totalVendido", totalVendido);
        m.put("totalEfectivo", totalEfectivo);
        m.put("totalAnulado", totalAnulado);
        m.put("ventas", ventasList);
        return m;
    }
    private Map<String, Object> cajaToMap(Caja c) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",            c.getId());
        m.put("estado",        c.getEstado());
        m.put("fechaApertura", c.getFechaApertura() != null ? c.getFechaApertura().format(fmt) : "?");
        m.put("fechaCierre",   c.getFechaCierre()   != null ? c.getFechaCierre().format(fmt)   : "?");
        m.put("montoInicial",  c.getMontoInicial());
        m.put("montoEsperado", c.getMontoEsperado());
        m.put("montoReal",     c.getMontoReal());
        m.put("diferencia",    c.getDiferencia());
        m.put("observaciones", c.getObservaciones());
        m.put("usuarioId",     c.getUsuario().getId());
        m.put("usuarioNombre", c.getUsuario().getNombres() + " " + c.getUsuario().getApellidos());
        m.put("sucursalId",    c.getSucursal().getId());
        m.put("sucursalNombre",c.getSucursal().getNombre());
        return m;
    }
    private BigDecimal orZero(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }
}