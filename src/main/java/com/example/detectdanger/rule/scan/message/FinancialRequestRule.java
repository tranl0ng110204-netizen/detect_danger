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
public class FinancialRequestRule implements DetectionRule {
    private static final int WEIGHT = 20;

    private static final Set<String> FINANCIAL_KEYWORDS =
            Set.of(
                    "chuyển khoản",
                    "chuyển tiền",
                    "gửi tiền",
                    "nạp tiền",
                    "thanh toán",
                    "phí xác minh",
                    "phí nhận thưởng",
                    "số tài khoản",
                    "stk",
                    "ngân hàng"
            );

    @Override
    public String getCode() {
        return "FINANCIAL_REQUEST";
    }

    @Override
    public String getName() {
        return "Financial Request Detection";
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
                FINANCIAL_KEYWORDS) {

            if (message.contains(keyword)) {

                matched.add(keyword);
            }
        }

        if (matched.isEmpty()) {

            return new RuleResult(
                    getCode(),
                    false,
                    0,
                    "No financial request detected",
                    List.of()
            );
        }

        return new RuleResult(
                getCode(),
                true,
                WEIGHT,
                "Financial request pattern detected",
                matched
        );
    }
}
