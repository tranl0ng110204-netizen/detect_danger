package com.example.detectdanger.rule.scan;

import java.util.List;

public record RuleResult(
        String ruleCode,
        boolean matches,
        int score,
        String reason,
        List<String> evidence
) {

}
