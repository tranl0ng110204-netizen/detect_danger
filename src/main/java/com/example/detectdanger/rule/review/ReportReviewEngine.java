package com.example.detectdanger.rule.review;

import com.example.detectdanger.rule.review.review_rules.ReportReviewRule;
import com.example.detectdanger.rule.review.review_rules.ReviewResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ReportReviewEngine {
    private final List<ReportReviewRule> rules;

    public List<ReviewResult> evaluate(
            ReportReviewContext context
    ){
        return rules.stream()
                .map(rule -> rule.evaluate(context))
                .toList();
    }

    public ReportReviewResult review(
            ReportReviewContext context
    ) {

        List<ReviewResult> results =
                evaluate(context);

        int totalScore =
                results.stream()
                        .mapToInt(ReviewResult::getScore)
                        .sum();

        ReviewRecommendation recommendation =
                determineRecommendation(totalScore);

        return ReportReviewResult.builder()
                .totalScore(totalScore)
                .recommendation(recommendation)
                .ruleResults(results)
                .build();
    }

    private ReviewRecommendation determineRecommendation(
            int score
    ) {

        if (score >= 40) {
            return ReviewRecommendation.ACCEPT_RECOMMEND;
        }

        if (score <= -20) {
            return ReviewRecommendation.REJECT_RECOMMEND;
        }

        return ReviewRecommendation.NEEDS_MANUAL_REVIEW;
    }
}
