package com.example.detectdanger.rule.review.review_rules;

import com.example.detectdanger.entity.Report;
import com.example.detectdanger.rule.review.ReportReviewContext;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Evidence implements ReportReviewRule {
    @Override
    public String getCode() {
        return "REPORT_EVIDENCE";
    }

    @Override
    public String getName() {
        return "Report Evidence Rule";
    }

    @Override
    public int getWeight() {
        return 25;
    }

    @Override
    public ReviewResult evaluate(
            ReportReviewContext context
    ) {

        Report report = context.getReport();
        String description = report.getReason();

        if (description == null || description.isBlank()) {
            return ReviewResult.builder()
                    .ruleCode(getCode())
                    .matched(false)
                    .score(-15)
                    .reason("Report has no description")
                    .evidence(List.of("No description provided"))
                    .build();
        }

        return ReviewResult.builder()
                .ruleCode(getCode())
                .matched(true)
                .score(25)
                .reason("Report contains supporting information")
                .evidence(List.of("Description provided"))
                .build();
    }

}
