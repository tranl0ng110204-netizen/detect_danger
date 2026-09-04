package com.example.detectdanger.rule.scan;

import com.example.detectdanger.entity.Enum.InputType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RuleEngine {
    private final List<DetectionRule> rules;


    public String getEngineVersion(InputType inputType){
        return rules.stream()
                .filter(r->r.getStatus() ==RuleStatus.ACTIVE && r.supports(inputType))
                .map(r->r.getCode() + ":" + r.getVersion() + ":" + r.getWeight())
                .sorted()
                .collect(Collectors.joining("|"));
    }
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
