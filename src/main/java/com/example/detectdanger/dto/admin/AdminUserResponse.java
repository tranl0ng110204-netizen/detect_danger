package com.example.detectdanger.dto.admin;

import com.example.detectdanger.entity.Enum.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class AdminUserResponse {
    private Long id;

    private String email;

    private String name;

    private Role role;

    private LocalDateTime createdAt;
}
