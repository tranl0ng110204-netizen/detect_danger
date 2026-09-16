package com.example.detectdanger.service.pdf;

import com.example.detectdanger.entity.Enum.InputType;
import com.example.detectdanger.entity.Enum.RiskLevel;
import com.example.detectdanger.exceptions.BusinessException;
import com.example.detectdanger.rule.scan.DynamicRuleEngine;
import com.example.detectdanger.rule.scan.RuleResult;
import com.example.detectdanger.service.RiskLevelCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDDestinationOrAction;
import org.apache.pdfbox.pdmodel.interactive.action.PDAction;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionJavaScript;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionLaunch;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfThreatScannerService {

    private static final int MAX_PAGES_TO_SCAN = 20;
    private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10MB
    private static final byte[] PDF_MAGIC_BYTES = "%PDF-".getBytes(StandardCharsets.US_ASCII);
    private static final Pattern URL_PATTERN = Pattern.compile(
            "https?://[a-zA-Z0-9.-]+(?:\\.[a-zA-Z]{2,})+(?::\\d+)?(?:/[^\\s]*)?",
            Pattern.CASE_INSENSITIVE
    );

    private final DynamicRuleEngine dynamicRuleEngine;
    private final RiskLevelCalculator riskLevelCalculator;

    public record PdfScanResult(
            int riskScore,
            RiskLevel riskLevel,
            List<String> evidences,
            int pageCount,
            int totalLinksFound
    ) {}

    public PdfScanResult scanPdf(MultipartFile file) {
        validateFile(file);

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            log.error("Không thể đọc byte của file PDF: {}", file.getOriginalFilename(), e);
            throw new BusinessException("Lỗi khi đọc tệp tin PDF tải lên");
        }

        validatePdfHeader(bytes);

        List<String> evidences = new ArrayList<>();
        Set<String> extractedUrls = new LinkedHashSet<>();
        int structuralRiskScore = 0;
        int pageCount;

        try (PDDocument document = Loader.loadPDF(bytes)) {
            pageCount = document.getNumberOfPages();
            int pagesToScan = Math.min(pageCount, MAX_PAGES_TO_SCAN);

            // 1. Phân tích cấu trúc (Structural threats)
            structuralRiskScore += inspectStructuralThreats(document, evidences);

            // 2. Bóc tách Hyperlinks từ các trang
            extractLinksFromPages(document, pagesToScan, extractedUrls, evidences);

            // 3. Bóc tách Text và tìm URL trần trong nội dung
            String extractedText = extractText(document, pagesToScan);
            extractUrlsFromText(extractedText, extractedUrls);

            // 4. Đánh giá các URL phát hiện được qua DynamicRuleEngine
            int urlRiskScore = evaluateUrls(extractedUrls, evidences);

            // 5. Đánh giá nội dung text qua DynamicRuleEngine (Message/Keyword rules)
            int textRiskScore = evaluateTextContent(extractedText, evidences);

            // 6. Tổng hợp điểm rủi ro
            int totalScore = Math.min(100, structuralRiskScore + urlRiskScore + textRiskScore);
            RiskLevel riskLevel = riskLevelCalculator.riskLevelCalculate(totalScore);

            if (evidences.isEmpty()) {
                evidences.add("Tài liệu PDF an toàn, không phát hiện mã nhúng hoặc liên kết đáng ngờ (đã kiểm tra " + pagesToScan + " trang)");
            }

            return new PdfScanResult(
                    totalScore,
                    riskLevel,
                    evidences,
                    pageCount,
                    extractedUrls.size()
            );

        } catch (IOException e) {
            log.error("Lỗi khi phân tích cấu trúc PDF: {}", file.getOriginalFilename(), e);
            throw new BusinessException("Tệp tin PDF bị lỗi cấu trúc hoặc bị hỏng, không thể phân tích");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Vui lòng tải lên một tệp PDF hợp lệ");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new BusinessException("Dung lượng tệp vượt quá giới hạn cho phép (tối đa 10MB)");
        }
    }

    private void validatePdfHeader(byte[] bytes) {
        if (bytes.length < PDF_MAGIC_BYTES.length) {
            throw new BusinessException("Tệp tải lên không phải là tệp PDF hợp lệ");
        }
        for (int i = 0; i < PDF_MAGIC_BYTES.length; i++) {
            if (bytes[i] != PDF_MAGIC_BYTES[i]) {
                throw new BusinessException("Tệp tải lên không đúng định dạng PDF hợp lệ (sai Magic Bytes)");
            }
        }
    }

    private int inspectStructuralThreats(PDDocument document, List<String> evidences) {
        int score = 0;
        PDDocumentCatalog catalog = document.getDocumentCatalog();
        if (catalog == null) {
            return 0;
        }

        PDDocumentNameDictionary names = catalog.getNames();

        // Kiểm tra JavaScript nhúng trong Catalog
        if (names != null && names.getJavaScript() != null) {
            score += 50;
            evidences.add("Phát hiện mã kịch bản JavaScript nhúng cấp độ tài liệu (/JavaScript)");
        }

        // Kiểm tra File nhúng ngầm (EmbeddedFiles)
        if (names != null && names.getEmbeddedFiles() != null) {
            score += 40;
            evidences.add("Phát hiện tệp tin đính kèm ẩn bên trong tài liệu PDF (/EmbeddedFiles)");
        }

        // Kiểm tra OpenAction nguy hiểm
        try {
            PDDestinationOrAction openAction = catalog.getOpenAction();
            if (openAction instanceof PDActionJavaScript) {
                score += 50;
                evidences.add("Phát hiện hành vi tự động thực thi JavaScript khi mở file (OpenAction: /JS)");
            } else if (openAction instanceof PDActionLaunch) {
                score += 65;
                evidences.add("Phát hiện lệnh khởi chạy phần mềm ngoại vi khi mở file (OpenAction: /Launch)");
            }
        } catch (IOException ignored) {
            // Không thể đọc OpenAction
        }

        return score;
    }

    private void extractLinksFromPages(PDDocument document, int pagesToScan, Set<String> extractedUrls, List<String> evidences) {
        for (int i = 0; i < pagesToScan; i++) {
            PDPage page = document.getPage(i);
            try {
                List<PDAnnotation> annotations = page.getAnnotations();
                for (PDAnnotation annotation : annotations) {
                    if (annotation instanceof PDAnnotationLink link) {
                        PDAction action = link.getAction();
                        if (action instanceof PDActionURI uriAction) {
                            String uri = uriAction.getURI();
                            if (uri != null && !uri.isBlank()) {
                                extractedUrls.add(uri.trim());
                            }
                        } else if (action instanceof PDActionLaunch) {
                            evidences.add("Trang " + (i + 1) + ": Chứa liên kết thực thi chương trình ngoại vi (/Launch)");
                        } else if (action instanceof PDActionJavaScript) {
                            evidences.add("Trang " + (i + 1) + ": Chứa liên kết kích hoạt mã kịch bản (/JavaScript)");
                        }
                    }
                }
            } catch (IOException e) {
                log.warn("Không thể đọc annotations trên trang {}", i + 1);
            }
        }
    }

    private String extractText(PDDocument document, int pagesToScan) {
        try {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(1);
            stripper.setEndPage(pagesToScan);
            return stripper.getText(document);
        } catch (IOException e) {
            log.warn("Không thể bóc tách text từ file PDF: {}", e.getMessage());
            return "";
        }
    }

    private void extractUrlsFromText(String text, Set<String> extractedUrls) {
        if (text == null || text.isBlank()) {
            return;
        }
        Matcher matcher = URL_PATTERN.matcher(text);
        while (matcher.find()) {
            extractedUrls.add(matcher.group().trim());
        }
    }

    private int evaluateUrls(Set<String> urls, List<String> evidences) {
        int urlScore = 0;
        for (String url : urls) {
            List<RuleResult> results = dynamicRuleEngine.evaluate(url, InputType.URL);
            for (RuleResult result : results) {
                if (result.matches()) {
                    urlScore += result.score();
                    evidences.add("Liên kết ẩn trong PDF [" + url + "]: " + result.reason());
                }
            }
        }
        return urlScore;
    }

    private int evaluateTextContent(String text, List<String> evidences) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        int textScore = 0;
        List<RuleResult> results = dynamicRuleEngine.evaluate(text, InputType.MESSAGE);
        for (RuleResult result : results) {
            if (result.matches()) {
                textScore += result.score();
                evidences.add("Nội dung văn bản trong PDF: " + result.reason());
            }
        }
        return textScore;
    }
}
