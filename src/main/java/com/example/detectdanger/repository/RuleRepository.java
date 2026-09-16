package com.example.detectdanger.repository;

import com.example.detectdanger.entity.Enum.InputType;
import com.example.detectdanger.entity.Enum.RuleStatus;
import com.example.detectdanger.entity.Rule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RuleRepository extends JpaRepository<Rule,Long> {
    List<Rule> findByInputTypeAndRuleStatus(InputType inputType, RuleStatus ruleStatus);

    boolean existsByRuleCode(String ruleCode);

    List<Rule> findAll();
}
