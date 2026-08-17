package com.example.detectdanger.service;

import com.example.detectdanger.dto.report.ReportRequest;
import com.example.detectdanger.dto.report.ReportResponse;
import com.example.detectdanger.entity.Report;
import com.example.detectdanger.entity.ReportStatus;
import com.example.detectdanger.entity.User;
import com.example.detectdanger.repository.ReportRepository;
import com.example.detectdanger.repository.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final NormalizeService normalizeService;
    private final ValidationService validationService;


    @Transactional
    public ReportResponse createReport(ReportRequest request, Authentication authentication){

        // lay email user trong Jwt
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("user not found"));

        String validatedInput = validationService.validate(request.inputType(),request.content());
        String normalizedInput = normalizeService.normalize(request.inputType(),request.content());

        Report report = new Report();
        report.setInputType(request.inputType());
        report.setNormalizedValue(normalizedInput);
        report.setReporterId(user.getId());
        report.setStatus(ReportStatus.PENDING);
        report.setReason(request.reason());
        Report savedReport = reportRepository.save(report);

        return new ReportResponse(
                savedReport.getId(),
                savedReport.getReporterId(),
                savedReport.getInputType(),
                savedReport.getStatus(),
                savedReport.getReason(),
                savedReport.getCreatedAt(),
                savedReport.getUpdatedAt()
        );


    }

    public List<ReportResponse> userReports(Authentication authentication){
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(()->new RuntimeException("user not found"));

        List<Report> userReports = reportRepository.findByReporterId(user.getId());

        return userReports.stream()
                .map(r -> new ReportResponse(
                        r.getId(),
                        r.getReporterId(),
                        r.getInputType(),
                        r.getStatus(),
                        r.getReason(),
                        r.getCreatedAt(),
                        r.getUpdatedAt()
                )).toList();

    }


}
