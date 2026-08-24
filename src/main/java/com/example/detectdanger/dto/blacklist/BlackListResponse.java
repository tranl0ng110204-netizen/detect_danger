package com.example.detectdanger.dto.blacklist;

import com.example.detectdanger.entity.BlackListSource;
import com.example.detectdanger.entity.InputType;

import java.time.LocalDateTime;

public record BlackListResponse(
        Long id,

        InputType inputType,

        String normalizedValue,

        String reason,

        BlackListSource source,

        boolean active,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}
