package com.example.detectdanger.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AdminReportResponse {
    String reason;
}
