package com.example.detectdanger.service.admin;

import com.example.detectdanger.dto.Rule.RuleResponse;
import com.example.detectdanger.entity.Rule;
import com.example.detectdanger.repository.RuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminRuleService {
    private final RuleRepository ruleRepository;


    @Transactional(readOnly = true)
    public List<RuleResponse> getAllRules(){
        List<Rule> rules = ruleRepository.findAll();
        return rules.stream()
                .map(this::ruleResponse)
                .toList();

    }

    private RuleResponse ruleResponse(Rule rule){
        return new RuleResponse(
                rule.getRuleName(),
                rule.getInputType(),
                rule.getRuleStatus(),
                rule.getRuleType(),
                rule.getWeight(),
                rule.isActive(),
                rule.getRuleValue(),
                rule.getVersion()
        );
    }

}
