package com.example.detectdanger.rule.scan.email;

import com.example.detectdanger.entity.Enum.InputType;
import com.example.detectdanger.rule.scan.DetectionRule;
import com.example.detectdanger.rule.scan.RuleResult;
import com.example.detectdanger.rule.scan.RuleStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SuspiciousEmailRule implements DetectionRule {
    private static final int WEIGHT = 20;

    private static final List<String> IMPERSONATION_PATTERNS =
            List.of(
                    "paypa1",
                    "micr0soft",
                    "faceb00k",
                    "g00gle",
                    "app1e"
            );

    @Override
    public String getCode() {
        return "SUSPICIOUS_EMAIL";
    }

    @Override
    public String getName() {
        return "Suspicious Email Detection";
    }

    @Override
    public int getWeight() {
        return WEIGHT;
    }

    @Override
    public RuleStatus getStatus() {
        return RuleStatus.ACTIVE;
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public boolean supports(InputType inputType) {
        return inputType == InputType.EMAIL;
    }

    @Override
    public RuleResult evaluate(
            String input,
            InputType inputType
    ) {

        String email = input.toLowerCase();

        List<String> evidence =
                IMPERSONATION_PATTERNS.stream()
                        .filter(email::contains)
                        .toList();

        if (evidence.isEmpty()) {

            return new RuleResult(
                    getCode(),
                    false,
                    0,
                    "No suspicious email pattern detected",
                    List.of()
            );
        }

        return new RuleResult(
                getCode(),
                true,
                WEIGHT,
                "Possible email impersonation detected",
                evidence
        );
    }
}
