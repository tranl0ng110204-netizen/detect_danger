package com.example.detectdanger.rule.scan.message;

import com.example.detectdanger.entity.Enum.InputType;
import com.example.detectdanger.rule.scan.DetectionRule;
import com.example.detectdanger.rule.scan.RuleResult;
import com.example.detectdanger.rule.scan.RuleStatus;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class SensitiveInformationRule implements DetectionRule {
    private static final int WEIGHT = 25;

    private static final List<String> SENSITIVE_KEYWORDS =
            List.of(
                    "otp",
                    "mật khẩu",
                    "password",
                    "mã xác nhận",
                    "mã bảo mật",
                    "cvv",
                    "số thẻ",
                    "mã otp"
            );

    @Override
    public String getCode() {
        return "SENSITIVE_INFORMATION_REQUEST";
    }

    @Override
    public String getName() {
        return "Sensitive Information Request Detection";
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
        return inputType == InputType.MESSAGE;
    }

    @Override
    public RuleResult evaluate(
            String input,
            InputType inputType
    ) {

        String message = input.toLowerCase();

        List<String> evidence =
                SENSITIVE_KEYWORDS.stream()
                        .filter(message::contains)
                        .toList();

        if (evidence.isEmpty()) {

            return new RuleResult(
                    getCode(),
                    false,
                    0,
                    "No sensitive information request detected",
                    List.of()
            );
        }

        return new RuleResult(
                getCode(),
                true,
                WEIGHT,
                "Sensitive information request detected",
                evidence
        );
    }

}
