package com.example.detectdanger.repository;

import com.example.detectdanger.entity.Report;
import com.example.detectdanger.entity.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report,Long> {

    List<Report> findByStatus(ReportStatus status);

    List<Report> findByReporterId(Long reporterId);

    Optional<Report> findById(Long id);
}
