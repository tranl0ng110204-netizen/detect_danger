package com.example.detectdanger.dto.moderator;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ModeratorDecisionRequest(
        @NotBlank(message = "reason is required")
        @Size(max = 1000, message = "reason must not exceed 1000 characters")
        String reason
) {
}
