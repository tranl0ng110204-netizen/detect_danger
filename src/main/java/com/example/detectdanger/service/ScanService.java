package com.example.detectdanger.service;

import com.example.detectdanger.dto.page.PageResponse;
import com.example.detectdanger.dto.scan.ScanRequest;
import com.example.detectdanger.dto.scan.ScanResponse;
import com.example.detectdanger.entity.Enum.RiskLevel;
import com.example.detectdanger.entity.Scan;
import com.example.detectdanger.entity.User;
import com.example.detectdanger.repository.ScanRepository;
import com.example.detectdanger.repository.UserRepository;
import com.example.detectdanger.rule.scan.RuleEngine;
import com.example.detectdanger.rule.scan.RuleResult;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;


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
    private final RuleEngine ruleEngine;

    @Transactional
    public ScanResponse createScan(ScanRequest request, Authentication authentication){

        //validate input
        validationService.validate(request.getInputType(),request.getContent());

        //normalize input
        normalizeService.normalize(request.getInputType(),request.getContent());

        String currentEngineVersion = ruleEngine.getEngineVersion(request.getInputType());

        Optional<Scan> existingScanCheck = scanRepository.findFirstByContentOrderByCreatedAtDesc(request.getContent());
        if(existingScanCheck.isPresent()){
            Scan existScan = existingScanCheck.get();

            if(currentEngineVersion.equals(existScan.getRuleVersion())){
                return toResponse(existScan);
            }
            return toResponse(reEvaluateExistScan(existScan,currentEngineVersion));
        }
        User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow();
        Scan scan = performNewScan(request,user,currentEngineVersion);
        return toResponse(scanRepository.save(scan));
    }

    @Transactional
    public ScanResponse reScanById(Long scanId){
        Scan scan = scanRepository.findById(scanId).orElseThrow(()-> new RuntimeException("Scan not found"));
        String currentEngineVersion = ruleEngine.getEngineVersion(scan.getInputType());
        Scan updated = reEvaluateExistScan(scan,currentEngineVersion);
        return toResponse(updated);
    }

    private Scan reEvaluateExistScan(Scan scan, String currentEngineVersion){
        List<RuleResult> results = ruleEngine.evaluate(scan.getContent(), scan.getInputType());
        int riskScore = riskScoreCalculate.calculate(results);
        RiskLevel riskLevel = riskLevelCalculator.riskLevelCalculate(riskScore);

        scan.setRiskLevel(riskLevel);
        scan.setRiskScore(riskScore);
        scan.setEvidence(results.stream().map(RuleResult::reason).toList());
        scan.setRuleVersion(currentEngineVersion);
        scan.setUpdatedAt(LocalDateTime.now());

        return scanRepository.save(scan);

    }

    private Scan performNewScan(ScanRequest request, User user, String engineVersion){
        List<RuleResult> results = ruleEngine.evaluate(request.getContent(),request.getInputType());
        int riskScore = riskScoreCalculate.calculate(results);
        RiskLevel riskLevel = riskLevelCalculator.riskLevelCalculate(riskScore);
        return Scan.builder()
                .user(user)
                .inputType(request.getInputType())
                .content(request.getContent())
                .riskScore(riskScore)
                .riskLevel(riskLevel)
                .ruleVersion(engineVersion)
                .evidence(results.stream().map(RuleResult::reason).toList())
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


    public List<ScanResponse> getHistoryScan(Authentication authentication){
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow();

        return scanRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();

    }

    public PageResponse<ScanResponse> getHistoryScanWithPanigation(Authentication authentication, int page, int size ){
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Direction.DESC,
                        "createdAt"
                )
        );
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow(()->new RuntimeException("user not found"));
        Page<Scan> history =  scanRepository.findByUserId(user.getId(),pageable);
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

    @Transactional
    public ScanResponse getScanById(Long scanId){
        Scan scan = scanRepository.findById(scanId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy kết quả scan với id: " + scanId));
        String currentEngineVersion = ruleEngine.getEngineVersion(scan.getInputType());
        if (scan.getRuleVersion() == null || !scan.getRuleVersion().equals(currentEngineVersion)) {
            scan = reEvaluateExistScan(scan, currentEngineVersion);
        }
        return toResponse(scan);
    }


}
