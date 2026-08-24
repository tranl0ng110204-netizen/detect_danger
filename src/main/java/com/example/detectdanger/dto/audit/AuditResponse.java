package com.example.detectdanger.dto.audit;

import com.example.detectdanger.entity.Enum.AuditAction;

import java.time.LocalDateTime;

public record AuditResponse(
        Long id,
        Long reportId,
        Long moderatorId,
        AuditAction action,
        String reason,
        LocalDateTime createAt
) {
}
