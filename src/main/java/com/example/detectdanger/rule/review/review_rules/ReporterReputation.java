package com.example.detectdanger.rule.review.review_rules;

import com.example.detectdanger.rule.review.ReportReviewContext;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReporterReputation implements ReportReviewRule {

    @Override
    public String getCode(){
        return "REPORTER_REPUTATION";
    }

    @Override
    public String getName(){
        return "Reporter Reputation Rule";
    }

    @Override
    public int getWeight(){
        return 20;

    }

    @Override
    public ReviewResult evaluate(ReportReviewContext context){
        int reputation =
                context.getReporterReputation();

        if (reputation >= 80) {

            return ReviewResult.builder()
                    .ruleCode(getCode())
                    .matched(true)
                    .score(20)
                    .reason(
                            "Reporter has high reputation"
                    )
                    .evidence(
                            List.of(
                                    "Reputation score: "
                                            + reputation
                            )
                    )
                    .build();
        }

        if (reputation < 30) {

            return ReviewResult.builder()
                    .ruleCode(getCode())
                    .matched(false)
                    .score(-20)
                    .reason(
                            "Reporter has low reputation"
                    )
                    .evidence(
                            List.of(
                                    "Reputation score: "
                                            + reputation
                            )
                    )
                    .build();
        }

        return ReviewResult.builder()
                .ruleCode(getCode())
                .matched(false)
                .score(0)
                .reason(
                        "Reporter has normal reputation"
                )
                .evidence(List.of())
                .build();
    }
}
