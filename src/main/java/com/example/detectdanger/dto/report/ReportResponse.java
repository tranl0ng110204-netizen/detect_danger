package com.example.detectdanger.dto.report;

import com.example.detectdanger.entity.Enum.InputType;
import com.example.detectdanger.entity.Enum.ReportStatus;
import com.example.detectdanger.rule.review.ReportReviewResult;

import java.time.LocalDateTime;

public record ReportResponse(
        Long id,

        Long userId,

        InputType inputType,

        ReportStatus status,

        String reason,

        ReportReviewResult reportReviewResult,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}
