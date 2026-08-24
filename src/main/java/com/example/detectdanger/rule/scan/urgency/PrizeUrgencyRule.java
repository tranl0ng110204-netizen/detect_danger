package com.example.detectdanger.rule.scan.urgency;

import com.example.detectdanger.entity.Enum.InputType;
import com.example.detectdanger.rule.scan.DetectionRule;
import com.example.detectdanger.rule.scan.RuleResult;
import com.example.detectdanger.rule.scan.RuleStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PrizeUrgencyRule implements DetectionRule {
    private static final int WEIGHT = 20;

    private static final List<String> PATTERNS = List.of(
            "trúng thưởng",
            "xác nhận ngay",
            "thực hiện ngay",
            "khẩn cấp",
            "chỉ còn",
            "hết hạn",
            "cơ hội cuối cùng"
    );

    @Override
    public String getCode() {
        return "PRIZE_URGENCY";
    }

    @Override
    public String getName() {
        return "Prize and Urgency Detection";
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
    public RuleResult evaluate(String input,InputType inputType) {
            List<String> matchedPatterns =
                    PATTERNS.stream()
                            .filter(input::contains)
                            .toList();

        if (matchedPatterns.isEmpty()) {

            return new RuleResult(
                    getCode(),
                    false,
                    0,
                    "No urgency pattern detected",
                    List.of()
            );
        }
        return new RuleResult(
                getCode(),
                true,
                WEIGHT,
                "Urgency or prize pattern detected",
                matchedPatterns
        );

    }

}
