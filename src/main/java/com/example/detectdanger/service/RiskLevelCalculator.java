package com.example.detectdanger.service;

import com.example.detectdanger.entity.Enum.RiskLevel;
import org.springframework.stereotype.Service;

@Service
public class RiskLevelCalculator {
    public RiskLevel riskLevelCalculate(int score){
        if (score >= 80) {
            return RiskLevel.CRITICAL;
        }

        if (score >= 60) {
            return RiskLevel.HIGH;
        }

        if (score >= 30) {
            return RiskLevel.MEDIUM;
        }

        return RiskLevel.LOW;
    }
}
