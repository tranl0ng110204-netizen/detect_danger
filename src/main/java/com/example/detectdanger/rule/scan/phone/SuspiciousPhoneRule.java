package com.example.detectdanger.rule.scan.phone;

import com.example.detectdanger.entity.Enum.InputType;
import com.example.detectdanger.rule.scan.DetectionRule;
import com.example.detectdanger.rule.scan.RuleResult;
import com.example.detectdanger.rule.scan.RuleStatus;
import org.springframework.stereotype.Component;

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
        return "1.0";
    }

    @Override
    public boolean supports(InputType inputType) {
        return inputType == InputType.PHONE;
    }

    @Override
    public RuleResult evaluate(
            String input,
            InputType InputType
    ) {

        String phone = input.replaceAll("\\D", "");

        if (phone.chars().distinct().count() == 1) {

            return new RuleResult(
                    getCode(),
                    true,
                    WEIGHT,
                    "Phone contains repeated digits",
                    List.of(phone)
            );
        }

        if (isSequential(phone)) {

            return new RuleResult(
                    getCode(),
                    true,
                    WEIGHT,
                    "Phone contains sequential digits",
                    List.of(phone)
            );
        }

        return new RuleResult(
                getCode(),
                false,
                0,
                "No suspicious phone pattern detected",
                List.of()
        );
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
