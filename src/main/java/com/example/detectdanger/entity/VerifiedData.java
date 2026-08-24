package com.example.detectdanger.entity;

import com.example.detectdanger.entity.Enum.InputType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "verified_data",uniqueConstraints = {
        @UniqueConstraint(name = "verify_data_value",
                columnNames = {"input_type","normalize_value"})
        })
@Getter
@Setter
public class VerifiedData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,name = "input_type")
    private InputType inputType;

    @Column(nullable = false, length = 2048,name = "normalize_value")
    private String normalizedValue;

    @Column(nullable = false,name = "source_report_id")
    private Long reportId;

    @Column(nullable = false)
    private Long verifiedBy;

    @Column(nullable = false)
    private LocalDateTime verifiedAt;

    @PrePersist
    protected void onCreate(){
        verifiedAt = LocalDateTime.now();
    }

}
