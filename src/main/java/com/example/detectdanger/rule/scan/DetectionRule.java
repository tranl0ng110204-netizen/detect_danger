package com.example.detectdanger.rule.scan;

import com.example.detectdanger.entity.Enum.InputType;


public interface DetectionRule {
    String getCode();

    String getName();

    String getVersion();

    boolean supports(InputType inputType);

    RuleResult evaluate(String input,InputType inputType);

    RuleStatus getStatus();

    int getWeight();


}
