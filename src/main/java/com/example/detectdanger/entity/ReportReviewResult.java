package com.example.detectdanger.entity;

import com.example.detectdanger.rule.review.ReviewRecommendation;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportReviewResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long reportId;

    @Column(nullable = false)
    private int totalScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewRecommendation recommendation;

    @Column(nullable = false)
    private List<String> ruleResult;


}
