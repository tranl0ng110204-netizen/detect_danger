package com.example.detectdanger.dto.moderator;

import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.Size;



public record ModeratorDecisionResponse(
        @NotBlank(message = "reason is required")
        @Size(max = 1000, message = "reason must not exceed over 1000 characters")
        String reason) {



}
