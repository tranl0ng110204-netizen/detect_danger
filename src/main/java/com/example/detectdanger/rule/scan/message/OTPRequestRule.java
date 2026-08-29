package com.example.detectdanger.rule.scan.message;

import com.example.detectdanger.entity.Enum.InputType;
import com.example.detectdanger.rule.scan.DetectionRule;
import com.example.detectdanger.rule.scan.RuleResult;
import com.example.detectdanger.rule.scan.RuleStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class OTPRequestRule implements DetectionRule {
    private static final int WEIGHT = 25;

    private static final Set<String> OTP_KEYWORDS =
            Set.of(
                    "otp",
                    "mã otp",
                    "mã xác thực",
                    "mã xác minh",
                    "verification code",
                    "mã bảo mật",
                    "mã xác nhận"
            );

    @Override
    public String getCode() {
        return "OTP_REQUEST";
    }

    @Override
    public String getName() {
        return "OTP Request Detection";
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

        if (input == null || input.isBlank()) {

            return new RuleResult(
                    getCode(),
                    false,
                    0,
                    "Message is empty",
                    List.of()
            );
        }

        String message =
                input.toLowerCase();

        List<String> matched =
                new ArrayList<>();

        for (String keyword :
                OTP_KEYWORDS) {

            if (message.contains(keyword)) {

                matched.add(keyword);
            }
        }

        if (matched.isEmpty()) {

            return new RuleResult(
                    getCode(),
                    false,
                    0,
                    "No OTP request detected",
                    List.of()
            );
        }

        return new RuleResult(
                getCode(),
                true,
                WEIGHT,
                "OTP or verification code request detected",
                matched
        );
    }
}
