package com.tato.motorepuestos.service;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
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
}