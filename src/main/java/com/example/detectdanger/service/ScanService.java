package com.example.detectdanger.service;
import com.example.detectdanger.dto.page.PageResponse;
import com.example.detectdanger.dto.scan.ScanRequest;
import com.example.detectdanger.dto.scan.ScanResponse;
import com.example.detectdanger.entity.Enum.InputType;
import com.example.detectdanger.entity.Enum.RiskLevel;
import com.example.detectdanger.entity.Scan;
import com.example.detectdanger.entity.User;
import com.example.detectdanger.exceptions.ResourceNotFoundException;
import com.example.detectdanger.repository.ScanRepository;
import com.example.detectdanger.repository.UserRepository;
import com.example.detectdanger.rule.scan.DynamicRuleEngine;
import com.example.detectdanger.rule.scan.RuleResult;
import com.example.detectdanger.service.pdf.PdfThreatScannerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ScanService {
    private final UserRepository userRepository;
    private final ScanRepository scanRepository;
    private final ValidationService validationService;
    private final NormalizeService normalizeService;
    private final RiskScoreCalculate riskScoreCalculate;
    private final RiskLevelCalculator riskLevelCalculator;
    private final DynamicRuleEngine dynamicRuleEngine;
    private final PdfThreatScannerService pdfThreatScannerService;

    @Transactional
    public ScanResponse createScan(ScanRequest request, Authentication authentication) {
        // 1. Validate & Normalize
        validationService.validate(request.getInputType(), request.getContent());
        String normalizedContent = normalizeService.normalize(request.getInputType(), request.getContent());

        // 2. Load rules 1 lần duy nhất — dùng cho cả version check lẫn evaluate
        DynamicRuleEngine.EngineSnapshot snapshot = dynamicRuleEngine.loadSnapshot(request.getInputType());

        // 3. Kiểm tra cache: content + inputType phải khớp để tránh nhầm loại scan
        Optional<Scan> existingScanCheck = scanRepository
                .findFirstByContentAndInputTypeOrderByCreatedAtDesc(normalizedContent, request.getInputType());

        if (existingScanCheck.isPresent()) {
            Scan existScan = existingScanCheck.get();

            // Nếu bộ Rule không thay đổi → trả về kết quả cũ (cache)
            if (snapshot.version().equals(existScan.getRuleVersion())) {
                return toResponse(existScan);
            }
            // Nếu rule đã thay đổi → quét lại bằng snapshot đã load
            return toResponse(reEvaluateExistScan(existScan, snapshot));
        }

        // 4. Lần đầu scan nội dung này
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + authentication.getName()));

        Scan scan = performNewScan(request.getInputType(), normalizedContent, user, snapshot);
        return toResponse(scanRepository.save(scan));
    }

    @Transactional
    public ScanResponse reScanById(Long scanId) {
        Scan scan = scanRepository.findById(scanId)
                .orElseThrow(() -> new ResourceNotFoundException("Scan not found with id: " + scanId));

        DynamicRuleEngine.EngineSnapshot snapshot = dynamicRuleEngine.loadSnapshot(scan.getInputType());
        return toResponse(reEvaluateExistScan(scan, snapshot));
    }

    @Transactional
    public ScanResponse getScanById(Long scanId, Authentication authentication) {
        Scan scan = scanRepository.findById(scanId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy kết quả scan với id: " + scanId));

        // Kiểm tra quyền sở hữu (IDOR protection)
        String currentUserEmail = authentication.getName();
        boolean isOwner = scan.getUser() != null
                && scan.getUser().getEmail().equalsIgnoreCase(currentUserEmail);
        boolean isStaff = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ROLE_MODERATOR"));

        if (!isOwner && !isStaff) {
            throw new AccessDeniedException("Bạn không có quyền xem kết quả quét này");
        }

        // Chỉ cần version string để so sánh cache — không load toàn bộ rules
        String currentEngineVersion = dynamicRuleEngine.getEngineVersion(scan.getInputType());

        if (scan.getRuleVersion() == null || !scan.getRuleVersion().equals(currentEngineVersion)) {
            // Rule đã thay đổi → load snapshot và re-evaluate
            DynamicRuleEngine.EngineSnapshot snapshot = dynamicRuleEngine.loadSnapshot(scan.getInputType());
            scan = reEvaluateExistScan(scan, snapshot);
        }

        return toResponse(scan);
    }

    @Transactional
    public ScanResponse scanFilePdf(MultipartFile file, Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        var scanResult = pdfThreatScannerService.scanPdf(file);
        String fileName = file.getOriginalFilename();
        // PDF scan dùng rules FILE (nếu không có rule FILE thì version = "")
        String currentEngineVersion = dynamicRuleEngine.getEngineVersion(InputType.FILE);

        Scan scan = Scan.builder()
                .user(user)
                .inputType(InputType.FILE)
                .content(fileName != null && !fileName.isBlank() ? fileName : "uploaded.pdf")
                .riskScore(scanResult.riskScore())
                .riskLevel(scanResult.riskLevel())
                .evidence(scanResult.evidences())
                .ruleVersion(currentEngineVersion)
                .build();

        return toResponse(scanRepository.save(scan));
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<ScanResponse> getHistoryScan(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + authentication.getName()));

        return scanRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public PageResponse<ScanResponse> getHistoryScanWithPanigation(Authentication authentication, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + authentication.getName()));

        Page<Scan> history = scanRepository.findByUserId(user.getId(), pageable);
        List<ScanResponse> historyPage = history.getContent().stream()
                .map(this::toResponse)
                .toList();

        return PageResponse.<ScanResponse>builder()
                .content(historyPage)
                .page(history.getNumber())
                .size(history.getSize())
                .totalElements(history.getTotalElements())
                .totalPages(history.getTotalPages())
                .last(history.isLast())
                .build();
    }

    private Scan reEvaluateExistScan(Scan scan, DynamicRuleEngine.EngineSnapshot snapshot) {
        List<RuleResult> results = dynamicRuleEngine.evaluate(scan.getContent(), snapshot);
        int riskScore = riskScoreCalculate.calculate(results);
        RiskLevel riskLevel = riskLevelCalculator.riskLevelCalculate(riskScore);
        List<String> matchedEvidences = extractReasons(results);

        scan.setRiskLevel(riskLevel);
        scan.setRiskScore(riskScore);
        scan.setEvidence(matchedEvidences);
        scan.setRuleVersion(snapshot.version());
        scan.setUpdatedAt(LocalDateTime.now());

        return scanRepository.save(scan);
    }

    private Scan performNewScan(InputType inputType, String normalizedContent, User user,
                                DynamicRuleEngine.EngineSnapshot snapshot) {
        List<RuleResult> results = dynamicRuleEngine.evaluate(normalizedContent, snapshot);
        int riskScore = riskScoreCalculate.calculate(results);
        RiskLevel riskLevel = riskLevelCalculator.riskLevelCalculate(riskScore);
        List<String> matchedEvidences = extractReasons(results);

        return Scan.builder()
                .user(user)
                .inputType(inputType)
                .content(normalizedContent)
                .riskScore(riskScore)
                .riskLevel(riskLevel)
                .ruleVersion(snapshot.version())
                .evidence(matchedEvidences)
                .build();
    }

    private List<String> extractReasons(List<RuleResult> results) {
        return results.stream()
                .filter(RuleResult::matches)
                .map(RuleResult::reason)
                .filter(reason -> reason != null && !reason.isBlank())
                .distinct()
                .toList();
    }

    private ScanResponse toResponse(Scan saved) {
        return ScanResponse.builder()
                .scanId(saved.getId())
                .content(saved.getContent())
                .inputType(saved.getInputType())
                .riskScore(saved.getRiskScore())
                .riskLevel(saved.getRiskLevel())
                .ruleResultList(saved.getEvidence())
                .createdAt(saved.getCreatedAt())
                .build();
    }
}