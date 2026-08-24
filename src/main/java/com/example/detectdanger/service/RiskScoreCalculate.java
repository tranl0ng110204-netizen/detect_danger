package com.example.detectdanger.service;

import com.example.detectdanger.rule.scan.RuleResult;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RiskScoreCalculate {
    private static final int MAX_SCORE = 100;
    private static final int VERIFIED_BLACKLIST_SCORE = 80;
    public int calculate(List<RuleResult> results) {

        if (results == null || results.isEmpty()) {
            return 0;
        }

        int totalScore = results.stream()
                .filter(RuleResult::matches)
                .mapToInt(RuleResult::score).
                sum();

        boolean blacklisted = results.stream()
                .anyMatch(result ->
                        result.matches()
                                && "BLACKLISTED_DATA"
                                .equals(result.ruleCode())
                );


        if (blacklisted) {
            totalScore = Math.max(
                    totalScore,
                    VERIFIED_BLACKLIST_SCORE
            );
        }

        /*
         * Không cho score vượt quá 100.
         */
        return Math.min(totalScore, MAX_SCORE);
    }
}
