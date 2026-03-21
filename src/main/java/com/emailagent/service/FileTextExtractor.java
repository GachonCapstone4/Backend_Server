package com.emailagent.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
public class FileTextExtractor {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final List<String> ALLOWED_TYPES = List.of("pdf", "docx", "txt");

    /**
     * 파일에서 텍스트 추출
     * 지원: PDF, DOCX, TXT
     */
    public String extract(MultipartFile file) throws IOException {
        validateFile(file);

        String extension = getExtension(file.getOriginalFilename()).toLowerCase();

        return switch (extension) {
            case "pdf"  -> extractFromPdf(file.getInputStream());
            case "docx" -> extractFromDocx(file.getInputStream());
            case "txt"  -> extractFromTxt(file.getInputStream());
            default -> throw new IllegalArgumentException("지원하지 않는 파일 형식입니다: " + extension);
        };
    }

    // =============================================
    // PDF 텍스트 추출 (Apache PDFBox)
    // =============================================
    private String extractFromPdf(InputStream inputStream) throws IOException {
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            log.debug("PDF 텍스트 추출 완료: {}자", text.length());
            return cleanText(text);
        }
    }

    // =============================================
    // DOCX 텍스트 추출 (Apache POI)
    // =============================================
    private String extractFromDocx(InputStream inputStream) throws IOException {
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            StringBuilder sb = new StringBuilder();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String text = paragraph.getText();
                if (text != null && !text.isBlank()) {
                    sb.append(text).append("\n");
                }
            }
            String text = sb.toString();
            log.debug("DOCX 텍스트 추출 완료: {}자", text.length());
            return cleanText(text);
        }
    }

    // =============================================
    // TXT 텍스트 추출
    // =============================================
    private String extractFromTxt(InputStream inputStream) throws IOException {
        String text = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        log.debug("TXT 텍스트 추출 완료: {}자", text.length());
        return cleanText(text);
    }

    // =============================================
    // 유효성 검사
    // =============================================
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기는 10MB를 초과할 수 없습니다.");
        }
        String extension = getExtension(file.getOriginalFilename()).toLowerCase();
        if (!ALLOWED_TYPES.contains(extension)) {
            throw new IllegalArgumentException(
                "지원하지 않는 파일 형식입니다. 지원 형식: PDF, DOCX, TXT");
        }
    }

    // =============================================
    // 유틸
    // =============================================
    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    /**
     * 텍스트 정제
     * - 연속된 빈 줄 제거
     * - 앞뒤 공백 제거
     */
    private String cleanText(String text) {
        if (text == null) return "";
        return text
                .replaceAll("\\r\\n", "\n")        // Windows 줄바꿈 통일
                .replaceAll("\\n{3,}", "\n\n")      // 연속 빈 줄 2개로 제한
                .trim();
    }
}
