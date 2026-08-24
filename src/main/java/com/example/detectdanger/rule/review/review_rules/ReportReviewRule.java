package com.example.detectdanger.rule.review.review_rules;

import com.example.detectdanger.rule.review.ReportReviewContext;

public interface ReportReviewRule {
    String getCode();

    String getName();

    int getWeight();

    ReviewResult evaluate(ReportReviewContext context);
}
