package com.example.detectdanger.rule.scan.email;

import com.example.detectdanger.entity.Enum.InputType;
import com.example.detectdanger.rule.scan.DetectionRule;
import com.example.detectdanger.rule.scan.RuleResult;
import com.example.detectdanger.rule.scan.RuleStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SuspiciousEmailRule implements DetectionRule {
    private static final int WEIGHT = 20;

    @Override
    public String getCode() {
        return "SUSPICIOUS_EMAIL";
    }

    @Override
    public String getName() {
        return "Suspicious Email Pattern Detection";
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
        return inputType == InputType.EMAIL;
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
                    "Email is empty",
                    List.of()
            );
        }

        String email = input.trim().toLowerCase();
        int atIndex = email.lastIndexOf("@");
        if (atIndex <= 0
                || atIndex == email.length() - 1) {
            evidence.add("Invalid email structure");

        } else {

            String localPart = email.substring(0, atIndex);
            String domain = email.substring(atIndex + 1);

            /*
             * Local part quá dài.
             */
            if (localPart.length() > 50) {

                evidence.add(
                        "Unusually long email local-part"
                );
            }

            /*
             * Có nhiều dấu chấm liên tiếp.
             */
            if (localPart.contains("..")) {

                evidence.add(
                        "Email local-part contains consecutive dots"
                );
            }

            /*
             * Domain có nhiều subdomain.
             */
            if (domain.split("\\.").length > 4) {

                evidence.add(
                        "Email domain contains unusually many subdomains"
                );
            }

            /*
             * Domain phải có dấu chấm.
             */
            if (!domain.contains(".")) {

                evidence.add(
                        "Email domain has no top-level domain"
                );
            }

            /*
             * Ký tự bất thường.
             */
            if (!localPart.matches(
                    "[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+"
            )) {

                evidence.add(
                        "Email local-part contains unusual characters"
                );
            }
        }

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
                "Suspicious email pattern detected",
                evidence
        );

    }
}
