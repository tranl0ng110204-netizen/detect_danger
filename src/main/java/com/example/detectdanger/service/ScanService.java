package com.example.detectdanger.service;

import com.example.detectdanger.dto.scan.ScanRequest;
import com.example.detectdanger.dto.scan.ScanResponse;
import com.example.detectdanger.entity.RiskLevel;
import com.example.detectdanger.entity.Scan;
import com.example.detectdanger.entity.User;
import com.example.detectdanger.repository.ScanRepository;
import com.example.detectdanger.repository.UserRepository;
import com.example.detectdanger.rule.RuleEngine;
import com.example.detectdanger.rule.RuleResult;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;


import java.util.List;

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

        //Check rule
        List<RuleResult> results = ruleEngine.evaluate(request.getContent(),request.getInputType());

        //risk score calculate
        int riskScore = riskScoreCalculate.calculate(results);

        //risk level calculate
        RiskLevel riskLevel = riskLevelCalculator.riskLevelCalculate(riskScore);


        User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow();
        Scan scan = Scan.builder()
                .user(user)
                .inputType(request.getInputType())
                .content(request.getContent())
                .riskScore(riskScore)
                .riskLevel(riskLevel)
                .evidence(results.stream().map(RuleResult::reason).toList())
                .build();

        Scan saved = scanRepository.save(scan);
        return toResponse(saved);
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

}
