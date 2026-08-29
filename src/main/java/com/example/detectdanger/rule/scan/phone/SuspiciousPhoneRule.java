package com.example.detectdanger.rule.scan.phone;

import com.example.detectdanger.entity.Enum.InputType;
import com.example.detectdanger.rule.scan.DetectionRule;
import com.example.detectdanger.rule.scan.RuleResult;
import com.example.detectdanger.rule.scan.RuleStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SuspiciousPhoneRule implements DetectionRule {
    private static final int WEIGHT = 20;

    @Override
    public String getCode() {
        return "SUSPICIOUS_PHONE";
    }

    @Override
    public String getName() {
        return "Suspicious Phone Detection";
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
        return "1.1";
    }

    @Override
    public boolean supports(InputType inputType) {
        return inputType == InputType.PHONE;
    }

    @Override
    public RuleResult evaluate(
            String input,
            InputType inputType
    ) {

        List<String> evidence =
                new ArrayList<>();

        if (input == null || input.isBlank()) {

            return new RuleResult(
                    getCode(),
                    false,
                    0,
                    "Phone number is empty",
                    List.of()
            );
        }

        String phone = normalizePhone(input);
        if (phone.isBlank()) {
            return new RuleResult(
                    getCode(),
                    true,
                    WEIGHT,
                    "Phone contains no numeric characters",
                    List.of(
                            "No numeric phone content found"
                    )
            );
        }
        // ==== cac so deu giong nhau
        if (allDigitsSame(phone)) {
            evidence.add("All phone digits are identical");
        }
        if (isSequential(phone)) {
            evidence.add(
                    "Phone contains an ascending or descending numeric sequence"
            );
        }

        if (evidence.isEmpty()) {
            return new RuleResult(
                    getCode(),
                    false,
                    0,
                    "No suspicious phone pattern detected",
                    List.of()
            );
        }

        return new RuleResult(
                getCode(),
                true,
                WEIGHT,
                "Suspicious phone pattern detected",
                evidence
        );
    }

    private String normalizePhone(String phone) {

        return phone.replaceAll(
                "[^0-9]",
                ""
        );
    }

    private boolean allDigitsSame(
            String phone
    ) {

        if (phone.length() < 6) {
            return false;
        }

        char first = phone.charAt(0);
        for (char digit : phone.toCharArray()) {
            if (digit != first) {
                return false;
            }
        }
        return true;
    }

    private boolean isSequential(String phone) {

        if (phone.length() < 2) {
            return false;
        }

        boolean increasing = true;
        boolean decreasing = true;

        for (int i = 1; i < phone.length(); i++) {

            int previous = phone.charAt(i - 1);
            int current = phone.charAt(i);

            if (current != previous + 1) {
                increasing = false;
            }

            if (current != previous - 1) {
                decreasing = false;
            }
        }

        return increasing || decreasing;
    }
}
