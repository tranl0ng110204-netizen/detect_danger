package com.example.detectdanger.dto.scan;

import com.example.detectdanger.entity.InputType;
import com.example.detectdanger.entity.RiskLevel;
import com.example.detectdanger.rule.RuleResult;
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
