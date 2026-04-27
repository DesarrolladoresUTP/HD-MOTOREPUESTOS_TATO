package com.tato.motorepuestos.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void enviarCorreoConArchivos(String destinatario, String asunto, String cuerpo,
                                        byte[] pdfBytes, String nombrePdf,
                                        byte[] xmlBytes, String nombreXml,
                                        byte[] cdrBytes, String nombreCdr) throws Exception {

        MimeMessage mensaje = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

        helper.setTo(destinatario);
        helper.setSubject(asunto);
        helper.setText(cuerpo, true);

        if (pdfBytes != null) helper.addAttachment(nombrePdf, new ByteArrayResource(pdfBytes));
        if (xmlBytes != null) helper.addAttachment(nombreXml, new ByteArrayResource(xmlBytes));
        if (cdrBytes != null) helper.addAttachment(nombreCdr, new ByteArrayResource(cdrBytes));

        mailSender.send(mensaje);
    }

    public void enviarCorreoConPdf(String destinatario, String asunto, String cuerpo, byte[] pdfBytes, String nombrePdf) throws Exception {
        enviarCorreoConArchivos(destinatario, asunto, cuerpo, pdfBytes, nombrePdf, null, null, null, null);
    }
}