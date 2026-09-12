package com.example.detectdanger.service;

import com.example.detectdanger.dto.page.PageResponse;
import com.example.detectdanger.dto.scan.ScanRequest;
import com.example.detectdanger.dto.scan.ScanResponse;
import com.example.detectdanger.entity.Enum.InputType;
import com.example.detectdanger.entity.Enum.RiskLevel;
import com.example.detectdanger.entity.Scan;
import com.example.detectdanger.entity.User;
import com.example.detectdanger.repository.ScanRepository;
import com.example.detectdanger.repository.UserRepository;
import com.example.detectdanger.rule.scan.DynamicRuleEngine;
import com.example.detectdanger.rule.scan.RuleResult;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.io.File;
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

    // CHỈ SỬ DỤNG DYNAMIC RULE ENGINE ĐỌC TỪ POSTGRESQL
    private final DynamicRuleEngine dynamicRuleEngine;

    @Transactional
    public ScanResponse createScan(ScanRequest request, Authentication authentication) {
        // 1. Validate & Normalize
        validationService.validate(request.getInputType(), request.getContent());
        normalizeService.normalize(request.getInputType(), request.getContent());

        // 2. Lấy version hiện tại của các rule trong PostgreSQL
        String currentEngineVersion = dynamicRuleEngine.getEngineVersion(request.getInputType());

        // 3. Kiểm tra xem nội dung đã từng được scan chưa
        Optional<Scan> existingScanCheck = scanRepository.findFirstByContentOrderByCreatedAtDesc(request.getContent());
        if (existingScanCheck.isPresent()) {
            Scan existScan = existingScanCheck.get();

            // Nếu bộ Rule trong DB không thay đổi -> trả về kết quả cũ (Cache)
            if (currentEngineVersion.equals(existScan.getRuleVersion())) {
                return toResponse(existScan);
            }
            // Nếu có rule mới được thêm/sửa trong PostgreSQL -> Quét lại và update DB
            return toResponse(reEvaluateExistScan(existScan, currentEngineVersion));
        }

        // 4. Nếu là lần đầu scan nội dung này
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found: " + authentication.getName()));

        Scan scan = performNewScan(request, user, currentEngineVersion);
        return toResponse(scanRepository.save(scan));
    }

    @Transactional
    public ScanResponse reScanById(Long scanId) {
        Scan scan = scanRepository.findById(scanId)
                .orElseThrow(() -> new RuntimeException("Scan not found with id: " + scanId));

        String currentEngineVersion = dynamicRuleEngine.getEngineVersion(scan.getInputType());
        Scan updated = reEvaluateExistScan(scan, currentEngineVersion);
        return toResponse(updated);
    }

    @Transactional
    public ScanResponse getScanById(Long scanId) {
        Scan scan = scanRepository.findById(scanId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy kết quả scan với id: " + scanId));

        String currentEngineVersion = dynamicRuleEngine.getEngineVersion(scan.getInputType());

        // Tự động re-scan nếu bộ Rule trong PostgreSQL đã có thay đổi kể từ lần quét trước
        if (scan.getRuleVersion() == null || !scan.getRuleVersion().equals(currentEngineVersion)) {
            scan = reEvaluateExistScan(scan, currentEngineVersion);
        }
        return toResponse(scan);
    }

    private Scan reEvaluateExistScan(Scan scan, String currentEngineVersion) {
        // Chạy lại với bộ rule mới nhất trong PostgreSQL
        List<RuleResult> results = dynamicRuleEngine.evaluate(scan.getContent(), scan.getInputType());
        int riskScore = riskScoreCalculate.calculate(results);
        RiskLevel riskLevel = riskLevelCalculator.riskLevelCalculate(riskScore);

        // Chỉ lưu bằng chứng/lý do của các rule THỰC SỰ BỊ VI PHẠM (matches == true)
        List<String> matchedEvidences = results.stream()
                .filter(RuleResult::matches)
                .map(RuleResult::reason)
                .toList();

        scan.setRiskLevel(riskLevel);
        scan.setRiskScore(riskScore);
        scan.setEvidence(matchedEvidences);
        scan.setRuleVersion(currentEngineVersion);
        scan.setUpdatedAt(LocalDateTime.now());

        return scanRepository.save(scan);
    }

    private Scan performNewScan(ScanRequest request, User user, String engineVersion) {
        List<RuleResult> results = dynamicRuleEngine.evaluate(request.getContent(), request.getInputType());
        int riskScore = riskScoreCalculate.calculate(results);
        RiskLevel riskLevel = riskLevelCalculator.riskLevelCalculate(riskScore);

        // Chỉ lưu bằng chứng/lý do của các rule THỰC SỰ BỊ VI PHẠM (matches == true)
        List<String> matchedEvidences = results.stream()
                .filter(RuleResult::matches)
                .map(RuleResult::reason)
                .toList();

        return Scan.builder()
                .user(user)
                .inputType(request.getInputType())
                .content(request.getContent())
                .riskScore(riskScore)
                .riskLevel(riskLevel)
                .ruleVersion(engineVersion)
                .evidence(matchedEvidences)
                .build();
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

    public List<ScanResponse> getHistoryScan(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return scanRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public PageResponse<ScanResponse> getHistoryScanWithPanigation(Authentication authentication, int page, int size) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

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

    public ScanResponse scanFilePdf(File file, Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        String fileName = file.getName();
        Scan scan = Scan.builder()
                .user(user)
                .inputType(InputType.FILE)
                .content(!fileName.isEmpty() ? fileName : "filename")
                .riskScore(0)
                .riskLevel(RiskLevel.LOW)
                .evidence(List.of("File PDF được tiếp nhận"))
                .ruleVersion(null)
                .createdAt(LocalDateTime.now())
                .build();
        scanRepository.save(scan);
        return toResponse(scan);
    }
}