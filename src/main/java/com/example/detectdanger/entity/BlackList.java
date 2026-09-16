package com.example.detectdanger.entity;

import com.example.detectdanger.entity.Enum.BlackListSource;
import com.example.detectdanger.entity.Enum.InputType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Table(name = "black_lists", uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_blacklist_input_value",
                columnNames = {
                        "input_type",
                        "normalized_value"
                }
        )
})
@Builder
public class BlackList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "input_type",
            nullable = false,
            length = 20
    )
    private InputType inputType;

    @Column(
            name = "normalized_value",
            nullable = false,
            length = 2000
    )
    private String normalizedValue;

    @Column(
            nullable = false,
            length = 500
    )
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private BlackListSource source;

    @Column(
            nullable = false
    )
    @Builder.Default
    private boolean active = true;

    @Column(
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
