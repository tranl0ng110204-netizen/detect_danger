package com.example.detectdanger.rule.review.review_rules;

import com.example.detectdanger.rule.review.ReportReviewContext;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AlreadyVerified implements ReportReviewRule {
    @Override
    public String getCode() {
        return "ALREADY_VERIFIED";
    }

    @Override
    public String getName() {
        return "Already Verified Rule";
    }

    @Override
    public int getWeight() {
        return 30;
    }

    @Override
    public ReviewResult evaluate(
            ReportReviewContext context
    ) {

        if (context.isInputAlreadyVerified()) {

            return ReviewResult.builder()
                    .ruleCode(getCode())
                    .matched(true)
                    .score(-30)
                    .reason(
                            "Input is already verified"
                    )
                    .evidence(
                            List.of(
                                    "Input already exists in verified set"
                            )
                    )
                    .build();
        }

        return ReviewResult.builder()
                .ruleCode(getCode())
                .matched(false)
                .score(0)
                .reason(
                        "Input is not already verified"
                )
                .evidence(List.of())
                .build();
    }
}
