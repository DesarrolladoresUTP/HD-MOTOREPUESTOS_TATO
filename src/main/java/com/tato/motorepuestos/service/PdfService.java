package com.tato.motorepuestos.service;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.tato.motorepuestos.model.Cotizacion;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
@Service
public class PdfService {
    public byte[] generarComprobantePdf(Map<String, Object> payload, boolean esProforma) throws Exception {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);
        document.open();
        Font fontTitulo    = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
        Font fontSubtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.DARK_GRAY);
        Font fontNormal    = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
        String tipoDoc   = esProforma ? "NOTA DE VENTA / PRE-CUENTA"
                : payload.get("tipoComprobante").toString().toUpperCase();
        String numeroDoc = esProforma ? "PROFORMA"
                : payload.get("serie").toString() + "-" + payload.get("numeroComprobante").toString();
        Paragraph titulo = new Paragraph("MOTOREPUESTOS TATO", fontTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);
        Paragraph subtitulo = new Paragraph(tipoDoc + " : " + numeroDoc, fontSubtitulo);
        subtitulo.setAlignment(Element.ALIGN_CENTER);
        subtitulo.setSpacingAfter(20);
        document.add(subtitulo);
        document.add(new Paragraph("Cliente: "        + safe(payload.get("nombreCliente"),    "Público General"), fontNormal));
        document.add(new Paragraph("Documento: "      + safe(payload.get("documentoCliente"), "00000000"),        fontNormal));
        document.add(new Paragraph("Método de Pago: " + (esProforma ? "Pendiente" : safe(payload.get("metodoPago"), "Efectivo")), fontNormal));
        document.add(new Paragraph(" "));
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.5f, 5f, 1.5f, 2f});
        String[] cabeceras = {"CÓDIGO", "DESCRIPCIÓN", "CANT.", "IMPORTE (S/)"};
        for (String cabecera : cabeceras) {
            PdfPCell cell = new PdfPCell(new Phrase(cabecera, fontSubtitulo));
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }
        List<Map<String, Object>> items = (List<Map<String, Object>>) payload.get("items");
        if (items != null && !items.isEmpty()) {
            for (Map<String, Object> item : items) {
                table.addCell(new Phrase(safe(item.get("codigo"), "-"),       fontNormal));
                table.addCell(new Phrase(safe(item.get("nombre"), "Producto"), fontNormal));
                PdfPCell cellCant = new PdfPCell(new Phrase(safe(item.get("cantidad"), "1"), fontNormal));
                cellCant.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cellCant);
                PdfPCell cellImporte = new PdfPCell(new Phrase(
                        String.format("%.2f", Double.parseDouble(safe(item.get("importe"), "0"))), fontNormal));
                cellImporte.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(cellImporte);
            }
        } else {
            table.addCell(new Phrase("-", fontNormal));
            table.addCell(new Phrase("Artículos de Venta", fontNormal));
            PdfPCell cellCant = new PdfPCell(new Phrase("1", fontNormal));
            cellCant.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cellCant);
            PdfPCell cellImporte = new PdfPCell(new Phrase(
                    String.format("%.2f", Double.parseDouble(payload.get("total").toString())), fontNormal));
            cellImporte.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(cellImporte);
        }
        document.add(table);
        document.add(new Paragraph(" "));
        Paragraph total = new Paragraph("TOTAL A PAGAR: S/ " +
                String.format("%.2f", Double.parseDouble(payload.get("total").toString())), fontTitulo);
        total.setAlignment(Element.ALIGN_RIGHT);
        document.add(total);
        document.close();
        return out.toByteArray();
    }
    private String safe(Object value, String fallback) {
        return (value != null && !value.toString().trim().isEmpty())
                ? value.toString().trim() : fallback;
    }
    public byte[] generarResumenCajasPdf(Map<String, Object> resumen) throws Exception {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);
        document.open();
        Font fTitulo    = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new BaseColor(3, 105, 161));
        Font fSubtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.DARK_GRAY);
        Font fNormal    = FontFactory.getFont(FontFactory.HELVETICA, 9,  BaseColor.BLACK);
        Font fBold      = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.BLACK);
        Font fTotal     = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, new BaseColor(3, 105, 161));
        Font fNeg       = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, new BaseColor(220, 38, 38));
        Font fPos       = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, new BaseColor(22, 163, 74));
        Font fSmall     = FontFactory.getFont(FontFactory.HELVETICA, 8,  BaseColor.GRAY);
        Paragraph titulo = new Paragraph("MOTOREPUESTOS TATO", fTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);
        Paragraph subtitulo = new Paragraph("RESUMEN DE GESTION DE CAJA", fSubtitulo);
        subtitulo.setAlignment(Element.ALIGN_CENTER);
        subtitulo.setSpacingAfter(4);
        document.add(subtitulo);
        Paragraph linea = new Paragraph("------------------------------------------------------------------------", fSmall);
        linea.setAlignment(Element.ALIGN_CENTER);
        linea.setSpacingAfter(10);
        document.add(linea);
        String nombreUsuario = safe(resumen.get("nombreUsuario"), "—");
        String rangoFechas   = safe(resumen.get("rangoFechas"),   "—");
        String sucursal      = safe(resumen.get("sucursal"),      "Todas");
        document.add(new Paragraph("Trabajador : " + nombreUsuario, fBold));
        document.add(new Paragraph("Periodo    : " + rangoFechas,   fNormal));
        document.add(new Paragraph("Sucursal   : " + sucursal,      fNormal));
        document.add(new Paragraph("Generado   : " + java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), fSmall));
        Paragraph sep = new Paragraph(" ");
        sep.setSpacingAfter(4);
        document.add(sep);
        PdfPTable tabla = new PdfPTable(7);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{1.8f, 2.2f, 2.2f, 1.8f, 1.8f, 1.8f, 1.8f});
        BaseColor headerColor = new BaseColor(3, 105, 161);
        String[] cabeceras = {"Estado", "Apertura", "Cierre", "Inicial (S/)", "Esperado (S/)", "Real (S/)", "Diferencia (S/)"};
        for (String cab : cabeceras) {
            PdfPCell cell = new PdfPCell(new Phrase(cab, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, BaseColor.WHITE)));
            cell.setBackgroundColor(headerColor);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            cell.setBorderColor(BaseColor.WHITE);
            tabla.addCell(cell);
        }
        List<Map<String, Object>> cajas = (List<Map<String, Object>>) resumen.get("cajas");
        BaseColor rowAlt = new BaseColor(240, 249, 255);
        int rowIdx = 0;
        for (Map<String, Object> c : cajas) {
            BaseColor bg = (rowIdx++ % 2 == 0) ? BaseColor.WHITE : rowAlt;
            String estado = safe(c.get("estado"), "—");
            BaseColor estadoColor = "ABIERTA".equals(estado) ? new BaseColor(22, 163, 74) : BaseColor.GRAY;
            PdfPCell cEstado = new PdfPCell(new Phrase(estado, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, estadoColor)));
            cEstado.setBackgroundColor(bg); cEstado.setHorizontalAlignment(Element.ALIGN_CENTER); cEstado.setPadding(4);
            tabla.addCell(cEstado);
            addCell(tabla, safe(c.get("fechaApertura"), "—"), fSmall, Element.ALIGN_CENTER, bg);
            addCell(tabla, safe(c.get("fechaCierre"), "—"), fSmall, Element.ALIGN_CENTER, bg);
            addCell(tabla, "S/ " + formatNum(c.get("montoInicial")), fNormal, Element.ALIGN_RIGHT, bg);
            addCell(tabla, "S/ " + formatNum(c.get("montoEsperado")), fNormal, Element.ALIGN_RIGHT, bg);
            addCell(tabla, "S/ " + formatNum(c.get("montoReal")), fNormal, Element.ALIGN_RIGHT, bg);
            double dif = parseDouble(c.get("diferencia"));
            Font fDif = dif < 0 ? fNeg : (dif > 0 ? fPos : fBold);
            String difTxt = (dif >= 0 ? "+" : "") + String.format("%.2f", dif);
            PdfPCell cDif = new PdfPCell(new Phrase(difTxt, fDif));
            cDif.setBackgroundColor(bg); cDif.setHorizontalAlignment(Element.ALIGN_RIGHT); cDif.setPadding(4);
            tabla.addCell(cDif);
        }
        document.add(tabla);
        document.add(new Paragraph(" "));
        PdfPTable totales = new PdfPTable(2);
        totales.setWidthPercentage(55);
        totales.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totales.setWidths(new float[]{3f, 2f});
        addResumenFila(totales, "Total cajas cerradas:",    safe(resumen.get("totalCajas"),    "0"),       fBold, fBold);
        addResumenFila(totales, "Total inicial acumulado:", "S/ " + formatNum(resumen.get("sumInicial")), fNormal, fNormal);
        addResumenFila(totales, "Total esperado acumulado:","S/ " + formatNum(resumen.get("sumEsperado")),fNormal, fNormal);
        addResumenFila(totales, "Total real acumulado:",    "S/ " + formatNum(resumen.get("sumReal")),    fNormal, fNormal);
        double difTotal = parseDouble(resumen.get("difTotal"));
        Font fDifTotal = difTotal < 0 ? fNeg : (difTotal > 0 ? fPos : fBold);
        String difTotalTxt = (difTotal >= 0 ? "+" : "") + String.format("%.2f", difTotal);
        addResumenFila(totales, "DIFERENCIA TOTAL:", "S/ " + difTotalTxt, fTotal, fDifTotal);
        document.add(totales);
        document.add(new Paragraph(" "));
        Paragraph nota = new Paragraph(
                "* Las diferencias negativas indican faltantes de efectivo respecto al monto esperado según las ventas registradas.\n" +
                        "* Las ventas anuladas no se incluyen en el cálculo del monto esperado.", fSmall);
        nota.setSpacingBefore(6);
        document.add(nota);
        document.close();
        return out.toByteArray();
    }

    public byte[] generarDetalleCajaPdf(Map<String, Object> detalle) throws Exception {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);
        document.open();
        Font fTitulo    = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new BaseColor(3, 105, 161));
        Font fSubtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.DARK_GRAY);
        Font fNormal    = FontFactory.getFont(FontFactory.HELVETICA, 9,  BaseColor.BLACK);
        Font fBold      = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.BLACK);
        Font fTotal     = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, new BaseColor(3, 105, 161));
        Font fNeg       = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, new BaseColor(220, 38, 38));
        Font fPos       = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, new BaseColor(22, 163, 74));
        Font fSmall     = FontFactory.getFont(FontFactory.HELVETICA, 8,  BaseColor.GRAY);
        Font fGris      = FontFactory.getFont(FontFactory.HELVETICA, 8,  BaseColor.GRAY);
        Paragraph titulo = new Paragraph("MOTOREPUESTOS TATO", fTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);
        Paragraph subtitulo = new Paragraph("DETALLE DE CAJA #" + safe(detalle.get("id"), "—"), fSubtitulo);
        subtitulo.setAlignment(Element.ALIGN_CENTER);
        subtitulo.setSpacingAfter(4);
        document.add(subtitulo);
        Paragraph linea = new Paragraph("------------------------------------------------------------------------", fSmall);
        linea.setAlignment(Element.ALIGN_CENTER);
        linea.setSpacingAfter(10);
        document.add(linea);
        // Datos generales
        document.add(new Paragraph("Trabajador : " + safe(detalle.get("usuarioNombre"), "—"), fBold));
        document.add(new Paragraph("Sucursal   : " + safe(detalle.get("sucursalNombre"), "—"), fNormal));
        String estado = safe(detalle.get("estado"), "—");
        Font fEstado = "ABIERTA".equals(estado) ? fPos : fGris;
        Paragraph pEstado = new Paragraph("Estado     : " + estado, fEstado);
        document.add(pEstado);
        document.add(new Paragraph("Apertura   : " + safe(detalle.get("fechaApertura"), "—"), fNormal));
        document.add(new Paragraph("Cierre     : " + safe(detalle.get("fechaCierre"), "—"), fNormal));
        if (detalle.get("observaciones") != null && !detalle.get("observaciones").toString().trim().isEmpty()) {
            document.add(new Paragraph("Obs.       : " + detalle.get("observaciones"), fNormal));
        }
        document.add(new Paragraph("Generado   : " + java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), fSmall));
        document.add(new Paragraph(" "));
        // Montos de caja
        PdfPTable montos = new PdfPTable(4);
        montos.setWidthPercentage(100);
        montos.setSpacingAfter(14);
        String[] cabMontos = {"Inicial (S/)", "Esperado (S/)", "Real (S/)", "Diferencia (S/)"};
        for (String cab : cabMontos) {
            PdfPCell cell = new PdfPCell(new Phrase(cab, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, BaseColor.WHITE)));
            cell.setBackgroundColor(new BaseColor(3, 105, 161));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            montos.addCell(cell);
        }
        addCell(montos, "S/ " + formatNum(detalle.get("montoInicial")), fNormal, Element.ALIGN_CENTER, BaseColor.WHITE);
        addCell(montos, "S/ " + formatNum(detalle.get("montoEsperado")), fNormal, Element.ALIGN_CENTER, BaseColor.WHITE);
        addCell(montos, "S/ " + formatNum(detalle.get("montoReal")), fNormal, Element.ALIGN_CENTER, BaseColor.WHITE);
        double dif = parseDouble(detalle.get("diferencia"));
        Font fDifMonto = dif < 0 ? fNeg : (dif > 0 ? fPos : fBold);
        PdfPCell cDif = new PdfPCell(new Phrase((dif >= 0 ? "+" : "") + String.format("%.2f", dif), fDifMonto));
        cDif.setHorizontalAlignment(Element.ALIGN_CENTER);
        cDif.setPadding(4);
        montos.addCell(cDif);
        document.add(montos);
        // Resumen de ventas
        PdfPTable resumenVentas = new PdfPTable(2);
        resumenVentas.setWidthPercentage(70);
        resumenVentas.setWidths(new float[]{3f, 2f});
        resumenVentas.setSpacingAfter(12);
        addResumenFila(resumenVentas, "Ventas registradas:", safe(detalle.get("cantVentas"), "0"), fBold, fBold);
        addResumenFila(resumenVentas, "Ventas anuladas:", safe(detalle.get("cantAnuladas"), "0"), fNormal, fNeg);
        addResumenFila(resumenVentas, "Total vendido (validas):", "S/ " + formatNum(detalle.get("totalVendido")), fNormal, fNormal);
        addResumenFila(resumenVentas, "Total en efectivo:", "S/ " + formatNum(detalle.get("totalEfectivo")), fNormal, fNormal);
        addResumenFila(resumenVentas, "Total anulado:", "S/ " + formatNum(detalle.get("totalAnulado")), fNormal, fNeg);
        document.add(resumenVentas);
        // Tabla de ventas
        Paragraph tituloVentas = new Paragraph("VENTAS REALIZADAS DURANTE LA CAJA", fSubtitulo);
        tituloVentas.setSpacingBefore(6);
        tituloVentas.setSpacingAfter(8);
        document.add(tituloVentas);
        List<Map<String, Object>> ventas = (List<Map<String, Object>>) detalle.get("ventas");
        if (ventas == null || ventas.isEmpty()) {
            document.add(new Paragraph("No se registraron ventas durante esta caja.", fSmall));
        } else {
            for (Map<String, Object> v : ventas) {
                boolean anulada = "ANULADA".equalsIgnoreCase(safe(v.get("estadoVenta"), ""));
                PdfPTable cabeceraVenta = new PdfPTable(1);
                cabeceraVenta.setWidthPercentage(100);
                cabeceraVenta.setSpacingBefore(6);
                BaseColor bgCab = anulada ? new BaseColor(254, 226, 226) : new BaseColor(224, 242, 254);
                Font fCab = anulada ? fNeg : fBold;
                String linea1 = safe(v.get("tipoComprobante"), "Comprobante") + " " + safe(v.get("numeroComprobante"), "")
                        + "   |   " + safe(v.get("fecha"), "")
                        + "   |   Cliente: " + safe(v.get("cliente"), "Público General")
                        + "   |   " + safe(v.get("metodoPago"), "")
                        + "   |   S/ " + formatNum(v.get("total"))
                        + "   |   " + (anulada ? "ANULADA" : safe(v.get("estadoVenta"), "—"))
                        + " / SUNAT: " + safe(v.get("estadoSunat"), "—");
                PdfPCell cCab = new PdfPCell(new Phrase(linea1, fCab));
                cCab.setBackgroundColor(bgCab);
                cCab.setPadding(5);
                cabeceraVenta.addCell(cCab);
                document.add(cabeceraVenta);
                List<Map<String, Object>> productos = (List<Map<String, Object>>) v.get("productos");
                if (productos != null && !productos.isEmpty()) {
                    PdfPTable tProd = new PdfPTable(4);
                    tProd.setWidthPercentage(96);
                    tProd.setWidths(new float[]{1.2f, 4f, 1f, 1.5f});
                    tProd.setSpacingBefore(2);
                    for (Map<String, Object> p : productos) {
                        addCell(tProd, safe(p.get("codigo"), "-"), fSmall, Element.ALIGN_LEFT, BaseColor.WHITE);
                        addCell(tProd, safe(p.get("nombre"), "-"), fSmall, Element.ALIGN_LEFT, BaseColor.WHITE);
                        addCell(tProd, safe(p.get("cantidad"), "-"), fSmall, Element.ALIGN_CENTER, BaseColor.WHITE);
                        addCell(tProd, "S/ " + formatNum(p.get("subtotal")), fSmall, Element.ALIGN_RIGHT, BaseColor.WHITE);
                    }
                    document.add(tProd);
                }
            }
        }
        document.add(new Paragraph(" "));
        Paragraph nota = new Paragraph(
                "* Las ventas anuladas se muestran con fondo rojo y no se incluyen en el monto esperado de caja.", fSmall);
        nota.setSpacingBefore(8);
        document.add(nota);
        document.close();
        return out.toByteArray();
    }
    private void addCell(PdfPTable t, String text, Font f, int align, BaseColor bg) {
        PdfPCell c = new PdfPCell(new Phrase(text, f));
        c.setBackgroundColor(bg);
        c.setHorizontalAlignment(align);
        c.setPadding(4);
        t.addCell(c);
    }
    private void addResumenFila(PdfPTable t, String label, String value, Font fLabel, Font fValue) {
        PdfPCell cL = new PdfPCell(new Phrase(label, fLabel));
        cL.setBorder(0); cL.setPadding(4);
        t.addCell(cL);
        PdfPCell cV = new PdfPCell(new Phrase(value, fValue));
        cV.setBorder(0); cV.setPadding(4); cV.setHorizontalAlignment(Element.ALIGN_RIGHT);
        t.addCell(cV);
    }
    private String formatNum(Object v) {
        if (v == null) return "0.00";
        try { return String.format("%.2f", Double.parseDouble(v.toString())); }
        catch (Exception e) { return "0.00"; }
    }
    private double parseDouble(Object v) {
        if (v == null) return 0.0;
        try { return Double.parseDouble(v.toString()); }
        catch (Exception e) { return 0.0; }
    }

    public byte[] generarCotizacionPdf(Cotizacion cot) throws Exception {
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);
        document.open();

        BaseColor rojoTato      = new BaseColor(227,  27,  35);
        BaseColor grisClaro     = new BaseColor(244, 244, 244);
        BaseColor grisMedio     = new BaseColor(85,   85,  85);
        BaseColor amarilloFondo = new BaseColor(255, 253, 245);
        BaseColor amarilloBorde = new BaseColor(245, 158,  11);

        Font fEmpresa    = FontFactory.getFont(FontFactory.HELVETICA_BOLD,  22, BaseColor.BLACK);
        Font fNormal     = FontFactory.getFont(FontFactory.HELVETICA,        9, grisMedio);
        Font fBold       = FontFactory.getFont(FontFactory.HELVETICA_BOLD,   9, BaseColor.BLACK);
        Font fTituloDoc  = FontFactory.getFont(FontFactory.HELVETICA_BOLD,  18, BaseColor.BLACK);
        Font fCodigo     = FontFactory.getFont(FontFactory.HELVETICA_BOLD,  14, rojoTato);
        Font fThLabel    = FontFactory.getFont(FontFactory.HELVETICA_BOLD,   8, BaseColor.BLACK);
        Font fTd         = FontFactory.getFont(FontFactory.HELVETICA,        9, BaseColor.BLACK);
        Font fGrandTotal = FontFactory.getFont(FontFactory.HELVETICA_BOLD,  14, BaseColor.BLACK);
        Font fSmall      = FontFactory.getFont(FontFactory.HELVETICA,        8, grisMedio);
        Font fTermLabel  = FontFactory.getFont(FontFactory.HELVETICA_BOLD,   9, new BaseColor(85, 85, 85));
        Font fTermItem   = FontFactory.getFont(FontFactory.HELVETICA,        8, new BaseColor(85, 85, 85));

        // ── HEADER ───────────────────────────────────────────────────────
        PdfPTable header = new PdfPTable(2);
        header.setWidthPercentage(100);
        header.setWidths(new float[]{1.2f, 1f});
        header.setSpacingAfter(20);

        PdfPCell cEmpresa = new PdfPCell();
        cEmpresa.setBorder(0);
        cEmpresa.setPaddingBottom(10);
        cEmpresa.addElement(new Paragraph("MOTOREPUESTOS TATO", fEmpresa));
        cEmpresa.addElement(new Paragraph("RUC: 20000000000", fNormal));
        cEmpresa.addElement(new Paragraph("Av. Principal 123, Ciudad", fNormal));
        cEmpresa.addElement(new Paragraph("Telefono: +51 987 654 321", fNormal));
        cEmpresa.addElement(new Paragraph("Correo: ventas@motorepuestostato.com", fNormal));
        header.addCell(cEmpresa);

        String fEmisionStr = cot.getFechaEmision()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        PdfPCell cDoc = new PdfPCell();
        cDoc.setBorder(0);
        cDoc.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cDoc.setPaddingBottom(10);
        Paragraph pTipoDoc = new Paragraph("COTIZACION", fTituloDoc);
        pTipoDoc.setAlignment(Element.ALIGN_RIGHT);
        cDoc.addElement(pTipoDoc);
        Paragraph pCodigo = new Paragraph(cot.getCodigo(), fCodigo);
        pCodigo.setAlignment(Element.ALIGN_RIGHT);
        cDoc.addElement(pCodigo);
        Paragraph pFecha = new Paragraph("Fecha de Emision: " + fEmisionStr, fNormal);
        pFecha.setAlignment(Element.ALIGN_RIGHT);
        cDoc.addElement(pFecha);
        header.addCell(cDoc);

        PdfPCell lineaCell = new PdfPCell(new Phrase(" "));
        lineaCell.setColspan(2);
        lineaCell.setBorderWidthBottom(1.5f);
        lineaCell.setBorderColorBottom(BaseColor.BLACK);
        lineaCell.setBorderWidthTop(0); lineaCell.setBorderWidthLeft(0); lineaCell.setBorderWidthRight(0);
        lineaCell.setPaddingBottom(4);
        header.addCell(lineaCell);
        document.add(header);

        // ── SECCION CLIENTE / CONDICIONES ────────────────────────────────
        String fVenceStr = cot.getFechaVencimiento()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        PdfPTable secCliente = new PdfPTable(2);
        secCliente.setWidthPercentage(100);
        secCliente.setWidths(new float[]{1f, 1f});
        secCliente.setSpacingAfter(20);

        PdfPCell boxCliente = new PdfPCell();
        boxCliente.setBackgroundColor(new BaseColor(250, 250, 250));
        boxCliente.setBorderColor(new BaseColor(238, 238, 238));
        boxCliente.setPadding(10);
        Paragraph tCliente = new Paragraph("FACTURAR A:", fThLabel);
        tCliente.setSpacingAfter(5);
        boxCliente.addElement(tCliente);
        boxCliente.addElement(new Paragraph("Cliente: " + safe(cot.getNombreCliente(), "Publico General"), fTd));
        boxCliente.addElement(new Paragraph("DNI/RUC: " + safe(cot.getDocumentoCliente(), "---"), fTd));
        secCliente.addCell(boxCliente);

        PdfPCell boxCond = new PdfPCell();
        boxCond.setBackgroundColor(new BaseColor(250, 250, 250));
        boxCond.setBorderColor(new BaseColor(238, 238, 238));
        boxCond.setPadding(10);
        Paragraph tCond = new Paragraph("CONDICIONES COMERCIALES:", fThLabel);
        tCond.setSpacingAfter(5);
        boxCond.addElement(tCond);
        boxCond.addElement(new Paragraph("Vigencia: 7 dias calendario.", fTd));
        Phrase pVence = new Phrase();
        pVence.add(new Chunk("Valido hasta: ", fTd));
        pVence.add(new Chunk(fVenceStr, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, rojoTato)));
        boxCond.addElement(new Paragraph(pVence));
        boxCond.addElement(new Paragraph("Moneda: Soles (PEN)", fTd));
        secCliente.addCell(boxCond);
        document.add(secCliente);

        // ── TABLA DE PRODUCTOS ───────────────────────────────────────────
        PdfPTable tabla = new PdfPTable(4);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{0.8f, 4f, 1.5f, 1.5f});
        tabla.setSpacingAfter(20);

        String[] cabeceras = {"Cant.", "Descripcion del Repuesto", "P. Unitario", "Importe"};
        for (String cab : cabeceras) {
            PdfPCell c = new PdfPCell(new Phrase(cab, fThLabel));
            c.setBackgroundColor(grisClaro);
            c.setPadding(8);
            c.setBorderColor(new BaseColor(221, 221, 221));
            c.setHorizontalAlignment(cab.equals("Cant.") ? Element.ALIGN_CENTER : Element.ALIGN_LEFT);
            tabla.addCell(c);
        }

        java.util.List<com.tato.motorepuestos.model.DetalleCotizacion> detalles = cot.getDetalles();
        for (com.tato.motorepuestos.model.DetalleCotizacion d : detalles) {
            PdfPCell cCant = new PdfPCell(new Phrase(String.valueOf(d.getCantidad()), fTd));
            cCant.setHorizontalAlignment(Element.ALIGN_CENTER);
            cCant.setPadding(8); cCant.setBorderColor(new BaseColor(221, 221, 221));
            tabla.addCell(cCant);

            PdfPCell cNombre = new PdfPCell(new Phrase(d.getNombreProducto(), fTd));
            cNombre.setPadding(8); cNombre.setBorderColor(new BaseColor(221, 221, 221));
            tabla.addCell(cNombre);

            PdfPCell cPrecio = new PdfPCell(new Phrase("S/ " + String.format("%.2f", d.getPrecioUnitario()), fTd));
            cPrecio.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cPrecio.setPadding(8); cPrecio.setBorderColor(new BaseColor(221, 221, 221));
            tabla.addCell(cPrecio);

            PdfPCell cSub = new PdfPCell(new Phrase("S/ " + String.format("%.2f", d.getSubtotal()), fTd));
            cSub.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cSub.setPadding(8); cSub.setBorderColor(new BaseColor(221, 221, 221));
            tabla.addCell(cSub);
        }
        document.add(tabla);

        // ── TOTALES ──────────────────────────────────────────────────────
        double totalVal    = cot.getTotal().doubleValue();
        double subtotalVal = totalVal / 1.18;
        double igvVal      = totalVal - subtotalVal;

        PdfPTable totalesWrap = new PdfPTable(2);
        totalesWrap.setWidthPercentage(100);
        totalesWrap.setWidths(new float[]{1.8f, 1f});
        totalesWrap.setSpacingAfter(20);

        PdfPCell cVacia = new PdfPCell(new Phrase(""));
        cVacia.setBorder(0);
        totalesWrap.addCell(cVacia);

        PdfPTable totales = new PdfPTable(2);
        totales.setWidthPercentage(100);
        totales.setWidths(new float[]{1.5f, 1f});
        addTotalFila(totales, "Subtotal:", String.format("S/ %.2f", subtotalVal), fBold, fTd, grisClaro, new BaseColor(221, 221, 221));
        addTotalFila(totales, "IGV (18%):", String.format("S/ %.2f", igvVal),     fBold, fTd, grisClaro, new BaseColor(221, 221, 221));

        PdfPCell cTotalLabel = new PdfPCell(new Phrase("TOTAL:", fGrandTotal));
        cTotalLabel.setBackgroundColor(new BaseColor(238, 238, 238));
        cTotalLabel.setPadding(8); cTotalLabel.setBorderColor(new BaseColor(221, 221, 221));
        cTotalLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totales.addCell(cTotalLabel);
        PdfPCell cTotalVal = new PdfPCell(new Phrase(String.format("S/ %.2f", totalVal), fGrandTotal));
        cTotalVal.setBackgroundColor(new BaseColor(238, 238, 238));
        cTotalVal.setPadding(8); cTotalVal.setBorderColor(new BaseColor(221, 221, 221));
        cTotalVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totales.addCell(cTotalVal);

        PdfPCell cTotalesCell = new PdfPCell(totales);
        cTotalesCell.setBorder(0);
        totalesWrap.addCell(cTotalesCell);
        document.add(totalesWrap);

        // ── TERMINOS Y CONDICIONES ───────────────────────────────────────
        PdfPTable terminos = new PdfPTable(1);
        terminos.setWidthPercentage(100);
        terminos.setSpacingAfter(20);
        PdfPCell cTerminos = new PdfPCell();
        cTerminos.setBackgroundColor(amarilloFondo);
        cTerminos.setBorderColorLeft(amarilloBorde);
        cTerminos.setBorderWidthLeft(4);
        cTerminos.setBorderWidthTop(0); cTerminos.setBorderWidthRight(0); cTerminos.setBorderWidthBottom(0);
        cTerminos.setPadding(10);
        cTerminos.addElement(new Paragraph("Terminos y Condiciones:", fTermLabel));
        cTerminos.addElement(new Paragraph("* Esta cotizacion no representa una reserva fisica de inventario.", fTermItem));
        cTerminos.addElement(new Paragraph("* Los precios estan sujetos a variacion si la cotizacion es presentada posterior a la fecha de vencimiento (" + fVenceStr + ").", fTermItem));
        cTerminos.addElement(new Paragraph("* La disponibilidad de los productos listados se confirmara en caja al momento de la compra fisica.", fTermItem));
        terminos.addCell(cTerminos);
        document.add(terminos);

        // ── FOOTER ───────────────────────────────────────────────────────
        Paragraph footer = new Paragraph(
                "Este documento es una proforma informativa y no posee validez tributaria ante SUNAT.\n" +
                        "Gracias por su preferencia!",
                fSmall);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(10);
        document.add(footer);

        document.close();
        return out.toByteArray();
    }

    private void addTotalFila(PdfPTable t, String label, String value, Font fLabel, Font fValue,
                              BaseColor bg, BaseColor borderColor) {
        PdfPCell cL = new PdfPCell(new Phrase(label, fLabel));
        cL.setBackgroundColor(bg); cL.setPadding(8);
        cL.setBorderColor(borderColor); cL.setHorizontalAlignment(Element.ALIGN_RIGHT);
        t.addCell(cL);
        PdfPCell cV = new PdfPCell(new Phrase(value, fValue));
        cV.setPadding(8); cV.setBorderColor(borderColor); cV.setHorizontalAlignment(Element.ALIGN_RIGHT);
        t.addCell(cV);
    }
}