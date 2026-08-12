package com.example.detectdanger.service;

import com.example.detectdanger.dto.scan.ScanRequest;
import com.example.detectdanger.dto.scan.ScanResponse;
import com.example.detectdanger.entity.RiskLevel;
import com.example.detectdanger.entity.Scan;
import com.example.detectdanger.entity.User;
import com.example.detectdanger.repository.ScanRepository;
import com.example.detectdanger.repository.UserRepository;
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


    public ScanResponse createScan(ScanRequest request, Authentication authentication){

        validationService.validate(request.getInputType(),request.getContent());

        normalizeService.normalize(request.getInputType(),request.getContent());


        User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow();
        Scan scan = Scan.builder()
                .user(user)
                .inputType(request.getInputType())
                .content(request.getContent())
                .riskScore(0)
                .riskLevel(RiskLevel.LOW)
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
