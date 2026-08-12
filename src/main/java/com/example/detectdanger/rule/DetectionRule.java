package com.example.detectdanger.rule;

import com.example.detectdanger.entity.InputType;
import org.springframework.stereotype.Component;


public interface DetectionRule {
    String getCode();

    String getName();

    String getVersion();

    boolean supports(InputType inputType);

    RuleResult evaluate(String input,InputType inputType);

    RuleStatus getStatus();

    int getWeight();


}
