package com.example.detectdanger.dto.error;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class ErrorResponse {
    private LocalDateTime timestamp;

    private int status;

    private String error;

    private String message;

    private String path;
}
