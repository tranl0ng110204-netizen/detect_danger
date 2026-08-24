package com.example.detectdanger.entity;

import com.example.detectdanger.entity.Enum.AuditAction;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "audits")
@Getter
@Setter

public class Audit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long reportId;

    @Column(nullable = false)
    private Long moderatorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction auditAction;

    @Column(nullable = false, length = 1000)
    private String reason;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
