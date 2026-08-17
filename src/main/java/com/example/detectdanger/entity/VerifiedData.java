package com.example.detectdanger.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "verified_data")
@Getter
@Setter
public class VerifiedData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InputType inputType;

    @Column(nullable = false, length = 500)
    private String normalizedValue;

    @Column(length = 1000)
    private String reason;

    @Column(nullable = false)
    private Long verifiedBy;

    @Column(nullable = false)
    private LocalDateTime verifiedAt;

}
