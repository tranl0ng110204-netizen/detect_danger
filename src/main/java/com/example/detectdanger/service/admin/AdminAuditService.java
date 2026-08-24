package com.example.detectdanger.service.admin;

import com.example.detectdanger.dto.audit.AuditResponse;
import com.example.detectdanger.dto.page.PageResponse;
import com.example.detectdanger.entity.Audit;
import com.example.detectdanger.repository.AuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class AdminAuditService {
    private final AuditRepository auditRepository;

    public PageResponse<AuditResponse> getAudits(int page, int size){
        validatePagination(page, size);
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Direction.DESC,
                        "createdAt"
                )
        );

        Page<Audit> auditPage = auditRepository.findAll(pageable);
        List<AuditResponse> responses = auditPage.getContent()
                .stream()
                .map(this::toResponse)
                .toList();
        return PageResponse.<AuditResponse>builder()
                .content(responses)
                .page(auditPage.getNumber())
                .size(auditPage.getSize())
                .totalElements(auditPage.getTotalElements())
                .totalPages(auditPage.getTotalPages())
                .last(auditPage.isLast())
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
    private AuditResponse toResponse(Audit audit){
            return new AuditResponse(
                audit.getId(),
                audit.getReportId(),
                audit.getModeratorId(),
                audit.getAuditAction(),
                audit.getReason(),
                audit.getCreatedAt()

        );
    }
}
