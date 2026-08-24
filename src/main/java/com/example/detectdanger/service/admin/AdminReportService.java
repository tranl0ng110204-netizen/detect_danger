package com.example.detectdanger.service.admin;

import com.example.detectdanger.dto.page.PageResponse;
import com.example.detectdanger.dto.report.ReportResponse;
import com.example.detectdanger.entity.Report;
import com.example.detectdanger.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminReportService {
    private final ReportRepository reportRepository;

    public PageResponse<ReportResponse> getReports(int page, int size){
        validatePagination(page, size);
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Direction.DESC,
                        "createdAt"
                )
        );
        Page<Report> reportPage = reportRepository.findAll(pageable);
        List<ReportResponse> responses = reportPage.getContent()
                .stream()
                .map(this::toResponse)
                .toList();
        return PageResponse.<ReportResponse>builder()
                .content(responses)
                .page(reportPage.getNumber())
                .size(reportPage.getSize())
                .totalElements(reportPage.getTotalElements())
                .totalPages(reportPage.getTotalPages())
                .last(reportPage.isLast())
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

    private ReportResponse toResponse(Report report){
        return new ReportResponse(
                report.getId(),
                report.getReporterId(),
                report.getInputType(),
                report.getStatus(),
                report.getReason(),
                null,
                report.getCreatedAt(),
                report.getUpdatedAt()
        );
    }
}
