package com.example.detectdanger.repository;

import com.example.detectdanger.entity.Scan;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScanRepository extends JpaRepository<Scan,Long> {
    List<Scan> findByUserIdOrderByCreatedAtDesc(Long userId);
    Page<Scan> findByUserId(Long userId, Pageable page);

    Optional<Scan> findById(Long id);

    Optional<Scan> findFirstByContentOrderByCreatedAtDesc(String content);

    boolean existsByContent(String content);
}
