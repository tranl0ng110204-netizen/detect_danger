package com.example.detectdanger.rule.review.review_rules;

import com.example.detectdanger.rule.review.ReportReviewContext;

import java.util.List;

public class AlreadyBlackListed implements ReportReviewRule{
    @Override
    public String getCode() {
        return "ALREADY_BLACKLISTED";
    }
    @Override
    public String getName() {
        return "Already Blacklisted Rule";
    }
    @Override
    public int getWeight() {
        return 50;
    }
    @Override
    public ReviewResult evaluate(ReportReviewContext context) {
        if (context.isInputAlreadyBlacklisted()) {
            return ReviewResult.builder()
                    .ruleCode(getCode())
                    .matched(true)
                    .score(-50) // Mục tiêu đã bị chặn rồi, từ chối report này để đỡ tốn công Admin
                    .reason("Nội dung này đã nằm trong danh sách đen từ trước")
                    .evidence(List.of("Target is already blacklisted"))
                    .build();
        }
        return ReviewResult.builder()
                .ruleCode(getCode())
                .matched(false)
                .score(0)
                .reason("Chưa có trong danh sách đen")
                .evidence(List.of())
                .build();
    }
}
