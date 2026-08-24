package com.example.detectdanger.dto.scan;

import com.example.detectdanger.entity.Enum.InputType;
import com.example.detectdanger.entity.Enum.RiskLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ScanResponse {
    private Long scanId;
    private InputType inputType;
    private String content;
    private Integer riskScore;
    private RiskLevel riskLevel;
    private List<String> ruleResultList;
    private LocalDateTime createdAt;
}
