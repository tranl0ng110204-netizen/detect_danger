package com.example.detectdanger.rule.review;

import com.example.detectdanger.rule.review.review_rules.ReviewResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class ReportCheckingResult {
    private int totalScore;

    private ReviewRecommendation recommendation;

    private List<ReviewResult> ruleResults;

}
