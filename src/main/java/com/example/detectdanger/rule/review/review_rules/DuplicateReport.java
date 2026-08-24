package com.example.detectdanger.rule.review.review_rules;

import com.example.detectdanger.rule.review.ReportReviewContext;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DuplicateReport implements ReportReviewRule {
    @Override
    public String getCode() {
        return "DUPLICATE_REPORT";
    }

    @Override
    public String getName() {
        return "Duplicate Report Rule";
    }

    @Override
    public int getWeight() {
        return 15;
    }

    @Override
    public ReviewResult evaluate(
            ReportReviewContext context
    ) {

        if (context.isDuplicateReport()) {

            return ReviewResult.builder()
                    .ruleCode(getCode())
                    .matched(true)
                    .score(15)
                    .reason(
                            "Similar reports already exist"
                    )
                    .evidence(
                            List.of(
                                    "Duplicate report detected"
                            )
                    )
                    .build();
        }

        return ReviewResult.builder()
                .ruleCode(getCode())
                .matched(false)
                .score(0)
                .reason(
                        "No duplicate report detected"
                )
                .evidence(List.of())
                .build();
    }
}
