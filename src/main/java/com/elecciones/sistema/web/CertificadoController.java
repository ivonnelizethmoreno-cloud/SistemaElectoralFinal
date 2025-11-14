package com.elecciones.sistema.web;

import com.elecciones.sistema.model.UserAccount;
import com.elecciones.sistema.repo.UserAccountRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@Controller
@RequiredArgsConstructor
public class CertificadoController {

    private final UserAccountRepository userAccountRepository;

    @GetMapping("/certificado/descargar")
    public ResponseEntity<InputStreamResource> descargarCertificado(Authentication auth) throws Exception {

        // =======================
        // 1. Datos del votante
        // =======================
        String cedula = "No registrado";
        String nombre = "Votante";

        if (auth != null) {
            UserAccount user = userAccountRepository.findByUsername(auth.getName());
            if (user != null) {
                cedula = user.getUsername();
                nombre = user.getNombreUsuario();
            }
        }

        // =======================
        // 2. Crear PDF
        // =======================
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A5.rotate(), 40, 40, 40, 40);
        PdfWriter writer = PdfWriter.getInstance(doc, out);
        doc.open();

        // === Colores y fuentes ===
        BaseColor azul = new BaseColor(20, 70, 160);
        Font titulo = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, azul);
        Font texto = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, BaseColor.BLACK);
        Font negrita = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.BLACK);
        Font firmaManuscrita = new Font(Font.FontFamily.COURIER, 16, Font.ITALIC, BaseColor.BLACK);

        // === Marco exterior ===
        PdfContentByte cb = writer.getDirectContent();
        cb.setColorStroke(azul);
        cb.setLineWidth(2f);
        cb.rectangle(20, 20, doc.getPageSize().getWidth() - 40, doc.getPageSize().getHeight() - 40);
        cb.stroke();

        // === Logo superior ===
        try (InputStream logoStream = new ClassPathResource("static/img/logo_rnec.png").getInputStream()) {
            Image logo = Image.getInstance(logoStream.readAllBytes());
            logo.scaleAbsolute(90, 50);
            logo.setAbsolutePosition(35, doc.getPageSize().getHeight() - 90);
            doc.add(logo);
        } catch (Exception e) {
            System.out.println("⚠ No se pudo cargar el logo: " + e.getMessage());
        }

        // === Encabezado ===
        Paragraph encabezado = new Paragraph("\nREGISTRADURÍA NACIONAL DEL ESTADO CIVIL\n", negrita);
        encabezado.setAlignment(Element.ALIGN_CENTER);
        doc.add(encabezado);

        Paragraph subtitulo = new Paragraph("CERTIFICADO ELECTORAL - ELECCIONES SENADO 2025\n\n", titulo);
        subtitulo.setAlignment(Element.ALIGN_CENTER);
        doc.add(subtitulo);

        // === Tabla información ===
        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(80);
        tabla.setSpacingBefore(20f);
        tabla.setSpacingAfter(20f);

        tabla.addCell(celda("Cédula No.", negrita));
        tabla.addCell(celda(cedula, texto));

        tabla.addCell(celda("Nombres y Apellidos", negrita));
        tabla.addCell(celda(nombre, texto));

        tabla.addCell(celda("Fecha de elección", negrita));
        tabla.addCell(celda("12 de Noviembre de 2025", texto));

        doc.add(tabla);

        // === Firma manuscrita inventada ===
        Paragraph firma = new Paragraph("\n\nJuan Pérez", firmaManuscrita);
        firma.setAlignment(Element.ALIGN_CENTER);
        doc.add(firma);

        Paragraph linea = new Paragraph("_______________________________", texto);
        linea.setAlignment(Element.ALIGN_CENTER);
        doc.add(linea);

        Paragraph cargo = new Paragraph("Jurado de votación", negrita);
        cargo.setAlignment(Element.ALIGN_CENTER);
        doc.add(cargo);

        // === Código de verificación ===
        doc.add(new Paragraph("\n\nCódigo de verificación: RNEC-" + (int) (Math.random() * 900000 + 100000), texto));

        doc.close();

        // === Descargar PDF ===
        ByteArrayInputStream bis = new ByteArrayInputStream(out.toByteArray());
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=certificado_votacion.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }

    private static PdfPCell celda(String texto, Font fuente) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, fuente));
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }
}
