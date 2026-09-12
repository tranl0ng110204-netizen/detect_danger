package com.example.detectdanger.rule.scan.phone;

import com.example.detectdanger.entity.Enum.InputType;
import com.example.detectdanger.rule.scan.DetectionRule;
import com.example.detectdanger.rule.scan.RuleResult;
import com.example.detectdanger.rule.scan.RuleStatus;

import java.util.List;

public class PhoneFormatRule implements DetectionRule {
    private static final int WEIGHT = 15;

    @Override
    public String getCode() {
        return "PHONE_FORMAT";
    }

    @Override
    public String getName() {
        return "Phone Format Detection";
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
            InputType inputType
    ) {

        if (input == null || input.isBlank()) {

            return new RuleResult(
                    getCode(),
                    true,
                    WEIGHT,
                    "Phone number is empty",
                    List.of(
                            "Phone number is empty"
                    )
            );
        }

        String phone = input.trim();
        boolean validVietnameseFormat =
                phone.matches("^0\\d{9}$")
                        || phone.matches("^\\+84\\d{9}$");

        if (!validVietnameseFormat) {
            return new RuleResult(
                    getCode(),
                    true,
                    WEIGHT,
                    "Invalid Vietnamese phone number format",
                    List.of(
                            "Phone does not match expected format"
                    )
            );
        }

        return new RuleResult(
                getCode(),
                false,
                0,
                "Phone format is valid",
                List.of()
        );
    }
}
