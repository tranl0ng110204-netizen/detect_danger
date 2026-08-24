package com.example.detectdanger.rule.scan;

import com.example.detectdanger.entity.Enum.InputType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RuleEngine {
    private final List<DetectionRule> rules;

    public List<RuleResult> evaluate(
            String input,
            InputType inputType
    ) {

        return rules.stream()
                .filter(rule ->
                        rule.getStatus() == RuleStatus.ACTIVE
                )
                .filter(rule ->
                        rule.supports(inputType)
                )
                .map(rule ->
                        rule.evaluate(input,inputType)
                )
                .toList();
    }

}
