package com.example.detectdanger.repository;

import com.example.detectdanger.entity.Enum.ReporterStatus;
import com.example.detectdanger.entity.Report;
import com.example.detectdanger.entity.Enum.ReportStatus;
import com.example.detectdanger.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report,Long> {

    List<Report> findByStatusIn(List<ReportStatus> statuses);

    List<Report> findByReporterId(Long reporterId);

    Optional<Report> findById(Long id);

    boolean existsByNormalizedValue(String normalizedValue);

    boolean existsByNormalizedValueAndStatus(String normalizedValue,  ReportStatus reportStatus);
}
