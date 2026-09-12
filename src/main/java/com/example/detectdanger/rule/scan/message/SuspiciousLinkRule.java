package com.example.detectdanger.rule.scan.message;

import com.example.detectdanger.entity.Enum.InputType;
import com.example.detectdanger.rule.scan.DetectionRule;
import com.example.detectdanger.rule.scan.RuleResult;
import com.example.detectdanger.rule.scan.RuleStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SuspiciousLinkRule implements DetectionRule {
    private static final int WEIGHT = 15;

    private static final Pattern URL_PATTERN =
            Pattern.compile(
                    "(?i)\\b((https?://)|(www\\.))[^\\s]+"
            );

    @Override
    public String getCode() {
        return "SUSPICIOUS_LINK";
    }

    @Override
    public String getName() {
        return "Suspicious Link Detection";
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

        Matcher matcher =
                URL_PATTERN.matcher(input);

        List<String> urls =
                new ArrayList<>();

        while (matcher.find()) {

            urls.add(
                    matcher.group()
            );
        }

        if (urls.isEmpty()) {

            return new RuleResult(
                    getCode(),
                    false,
                    0,
                    "No URL detected in message",
                    List.of()
            );
        }
        return new RuleResult(
                getCode(),
                true,
                WEIGHT,
                "URL detected inside message",
                List.of(
                        "Message contains one or more URLs"
                )
        );
    }
}
