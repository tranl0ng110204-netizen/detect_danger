package com.example.detectdanger.entity;

import com.example.detectdanger.entity.Enum.Role;
import com.example.detectdanger.entity.Enum.UserStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus userStatus;

    @Column(nullable = false)
    @Builder.Default
    private Integer reputationScore = 100;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalReports = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer rejectedReports = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer verifiedReports = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean isDelete = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }



}
