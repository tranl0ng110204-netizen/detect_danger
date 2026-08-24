package com.example.detectdanger.rule.review.review_rules;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class ReviewResult {
    private String ruleCode;

    private boolean matched;

    private int score;

    private String reason;

    private List<String> evidence;


}
