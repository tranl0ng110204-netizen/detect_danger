package com.example.detectdanger.dto.report;

import com.example.detectdanger.entity.InputType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReportRequest(
        @NotNull(message = "Input type is required")
        InputType inputType,

        @NotBlank(message = "Content is required")
        @Size(
                max = 5000,
                message = "Content must not exceed 5000 characters"
        )
        String content,

        @Size(
                max = 1000,
                message = "Reason must not exceed 1000 characters"
        )
        String reason
) {


}
