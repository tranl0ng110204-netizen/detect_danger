package com.example.detectdanger.repository;

import com.example.detectdanger.entity.Enum.InputType;
import com.example.detectdanger.entity.Scan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScanRepository extends JpaRepository<Scan, Long> {
    List<Scan> findByUserIdOrderByCreatedAtDesc(Long userId);
    Page<Scan> findByUserId(Long userId, Pageable page);

    // Cache lookup: khớp cả content VÀ inputType để tránh trả nhầm kết quả
    Optional<Scan> findFirstByContentAndInputTypeOrderByCreatedAtDesc(String content, InputType inputType);
}
