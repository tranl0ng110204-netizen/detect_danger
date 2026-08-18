package com.example.detectdanger.dto.verify_data;

import com.example.detectdanger.entity.InputType;

import java.time.LocalDateTime;

public record VerifyDataResponse(
        Long id,

        InputType inputType,

        String normalizedValue,

        Long sourceReportId,

        Long verifiedBy,

        LocalDateTime verifiedAt
) {
}
