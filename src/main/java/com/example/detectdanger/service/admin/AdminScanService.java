package com.example.detectdanger.service.admin;

import com.example.detectdanger.dto.page.PageResponse;
import com.example.detectdanger.dto.scan.ScanResponse;
import com.example.detectdanger.entity.Scan;
import com.example.detectdanger.repository.ScanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminScanService {
    private final ScanRepository scanRepository;

    public PageResponse<ScanResponse> getScans(int page ,int size){
        validatePagination(page, size);
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Direction.DESC,
                        "createdAt"
                )
        );
        Page<Scan> scanPage = scanRepository.findAll(pageable);
        List<ScanResponse> responses = scanPage.getContent()
                .stream()
                .map(this::toResponse)
                .toList();
        return PageResponse.<ScanResponse>builder()
                .content(responses)
                .page(scanPage.getNumber())
                .size(scanPage.getSize())
                .totalElements(scanPage.getTotalElements())
                .totalPages(scanPage.getTotalPages())
                .last(scanPage.isLast())
                .build();

    }

    private ScanResponse toResponse(Scan scan){
        return ScanResponse.builder()
                .scanId(scan.getId())
                .content(scan.getContent())
                .inputType(scan.getInputType())
                .riskScore(scan.getRiskScore())
                .riskLevel(scan.getRiskLevel())
                .ruleResultList(scan.getEvidence())
                .createdAt(scan.getCreatedAt())
                .build();
    }
    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page must not be negative"
            );
        }

        if (size < 1 || size > 50) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 50"
            );
        }
    }
}
