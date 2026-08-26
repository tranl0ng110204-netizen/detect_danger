package com.example.detectdanger.repository;

import com.example.detectdanger.entity.ReportReviewResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReportReviewResultRepository extends JpaRepository<ReportReviewResult,Long> {
    Optional<ReportReviewResult> findById(Long id);

    ReportReviewResult findByReportId(Long id);
}
