package com.example.detectdanger.repository;

import com.example.detectdanger.entity.Audit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditRepository extends JpaRepository<Audit,Long> {
    List<Audit> findByReportIdOrderByCreatedAtAsc(Long id);
}
