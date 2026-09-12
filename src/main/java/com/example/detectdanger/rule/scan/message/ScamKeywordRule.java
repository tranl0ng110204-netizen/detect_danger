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
public class ScamKeywordRule implements DetectionRule {

    private static final int WEIGHT = 15;

    private static final Set<String> SCAM_KEYWORDS =
            Set.of(
                    "trúng thưởng",
                    "trúng giải",
                    "nhận thưởng",
                    "quà tặng",
                    "tài khoản bị khóa",
                    "xác minh tài khoản",
                    "phần thưởng",
                    "nhận tiền"
            );

    @Override
    public String getCode() {
        return "SCAM_KEYWORD";
    }

    @Override
    public String getName() {
        return "Scam Keyword Detection";
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

        List<String> matchedKeywords =
                new ArrayList<>();

        for (String keyword :
                SCAM_KEYWORDS) {

            if (message.contains(keyword)) {
                matchedKeywords.add(keyword);
            }
        }

        if (matchedKeywords.isEmpty()) {
            return new RuleResult(
                    getCode(),
                    false,
                    0,
                    "No scam-related keywords detected",
                    List.of()
            );
        }

        return new RuleResult(
                getCode(),
                true,
                WEIGHT,
                "Potential scam-related keywords detected",
                matchedKeywords
        );
    }
}
