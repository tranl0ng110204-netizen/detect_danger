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
public class UrgencyRule implements DetectionRule {
    private static final int WEIGHT = 10;

    private static final Set<String> URGENCY_KEYWORDS =
            Set.of(
                    "ngay lập tức",
                    "lập tức",
                    "khẩn cấp",
                    "ngay bây giờ",
                    "ngay hôm nay",
                    "trong 5 phút",
                    "trong 10 phút",
                    "gấp",
                    "không được chậm trễ"
            );

    @Override
    public String getCode() {
        return "URGENCY_DETECT";
    }

    @Override
    public String getName() {
        return "Urgency Language Detection";
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
                URGENCY_KEYWORDS) {
            if (message.contains(keyword)) {
                matched.add(keyword);
            }
        }

        if (matched.isEmpty()) {

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
                "Urgency language detected",
                matched
        );
    }
}
