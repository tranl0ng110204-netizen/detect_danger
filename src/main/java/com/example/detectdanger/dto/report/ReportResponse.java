package com.example.detectdanger.dto.report;

import com.example.detectdanger.entity.InputType;
import com.example.detectdanger.entity.ReportStatus;

import java.time.LocalDateTime;

public record ReportResponse(
        Long id,

        Long userId,

        InputType inputType,

        ReportStatus status,

        String reason,


        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}
