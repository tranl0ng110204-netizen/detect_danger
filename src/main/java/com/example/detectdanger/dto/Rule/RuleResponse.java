package com.example.detectdanger.dto.Rule;

import com.example.detectdanger.entity.Enum.InputType;
import com.example.detectdanger.entity.Enum.RuleStatus;
import com.example.detectdanger.entity.Enum.RuleType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RuleResponse {
    private String ruleName;
    private InputType inputType;
    private RuleStatus ruleStatus;
    private RuleType ruleType;
    private Integer weight;
    private boolean isActive;
    private String ruleValue;
    private String version;

}
