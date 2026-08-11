package com.example.detectdanger.dto.scan;

import com.example.detectdanger.entity.InputType;
import com.example.detectdanger.entity.RiskLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ScanResponse {
    private Long scanId;
    private InputType inputType;
    private Integer riskScore;
    private RiskLevel riskLevel;
    private LocalDateTime createdAt;
}
