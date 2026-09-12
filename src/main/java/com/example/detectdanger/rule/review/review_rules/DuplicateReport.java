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
        if (!context.isDuplicateReport()) {
            return ReviewResult.builder()
                    .ruleCode(getCode())
                    .matched(false)
                    .score(0)
                    .reason("Báo cáo đầu tiên cho mục tiêu này")
                    .evidence(List.of())
                    .build();
        }
        int reporterReputation = context.getReporterReputation();
        // TH1: Bị nhiều người report, nhưng người này uy tín thấp (< 40)
        // -> Rất có thể là clone tham gia đợt dìm hàng -> TRỪ ĐIỂM
        if (reporterReputation < 40) {
            return ReviewResult.builder()
                    .ruleCode(getCode())
                    .matched(true)
                    .score(-25) // Phạt nặng
                    .reason("Phát hiện trùng lặp từ tài khoản uy tín thấp (Nghi vấn dìm hàng / Sybil Attack)")
                    .evidence(List.of("Low reputation reporter duplicating reports"))
                    .build();
        }
        // TH2: Trùng lặp nhưng đến từ tài khoản uy tín cao -> CỘNG ĐIỂM xác thực
        return ReviewResult.builder()
                .ruleCode(getCode())
                .matched(true)
                .score(15)
                .reason("Nhiều người dùng uy tín cùng phản ánh về mục tiêu này")
                .evidence(List.of("Confirmed by reputable community members"))
                .build();
    }
}
