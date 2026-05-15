package org.dam.search.backend.domain.services;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.dam.search.backend.domain.entities.Document;
import org.dam.search.backend.domain.projections.ImportedDocument;
import org.dam.search.backend.domain.repository.DocumentRepository;
import org.dam.search.backend.utils.TextNormalizer;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class DocumentService extends BaseCRUDService<Document, Long> {

    public DocumentService(DocumentRepository repository) {
        super(repository);
    }

    public ImportedDocument importDocument(Path path) throws IOException {
        String fileName = path.getFileName().toString();
        String ext = getExtension(fileName);
        String rawText = switch (ext) {
            case "pdf" -> extractPdf(path);
            case "docx" -> extractDocx(path);
            default -> throw new IllegalArgumentException("Unsupported file type: " + ext);
        };

        String normalized = TextNormalizer.normalizeText(rawText);

        return ImportedDocument.builder()
                .title(fileName)
                .path(path.toAbsolutePath().toString())
                .rawText(rawText)
                .contentHash(sha256(normalized))
                .normalizedText(normalized)
                .build();
    }

    private String getExtension(String fileName) {
        int i = fileName.lastIndexOf('.');
        if (i < 0 || i == fileName.length() - 1) return "";
        return fileName.substring(i + 1).toLowerCase();
    }


    private static String extractPdf(Path path) throws IOException {
        try (PDDocument doc = Loader.loadPDF(path.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc);
        }
    }

    private static String extractDocx(Path path) throws IOException {
        try (InputStream is = Files.newInputStream(path);
             XWPFDocument doc = new XWPFDocument(is);
             XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
            return extractor.getText();
        }
    }

    private static String sha256(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(dig.length * 2);
            for (byte b : dig) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
