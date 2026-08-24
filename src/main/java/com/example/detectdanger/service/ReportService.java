package com.example.detectdanger.service;

import com.example.detectdanger.dto.report.ReportRequest;
import com.example.detectdanger.dto.report.ReportResponse;
import com.example.detectdanger.entity.Enum.ReporterStatus;
import com.example.detectdanger.entity.Report;
import com.example.detectdanger.entity.Enum.ReportStatus;
import com.example.detectdanger.entity.User;
import com.example.detectdanger.repository.ReportRepository;
import com.example.detectdanger.repository.UserRepository;
import com.example.detectdanger.rule.review.ReportReviewResult;
import com.example.detectdanger.service.moderator.ReporterReputationService;
import com.example.detectdanger.service.moderator.ReporterRiskService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final NormalizeService normalizeService;
    private final ValidationService validationService;
    private final ReporterRiskService reporterRiskService;


    @Transactional
    public ReportResponse createReport(ReportRequest request, Authentication authentication){

        // lay email user trong Jwt
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("user not found"));

        String validatedInput = validationService.validate(request.inputType(),request.content());
        String normalizedInput = normalizeService.normalize(request.inputType(),request.content());

        boolean exists = reportRepository.existsByNormalizedValue(normalizedInput);
        if(exists){
            throw new IllegalArgumentException("noi dung report da co trong danh sach report");
        }

        ReporterStatus reporterStatus = reporterRiskService.evaluateUser(user);
        if(reporterStatus == ReporterStatus.RESTRICTED){
            throw new RuntimeException("tai khoan cua ban co dau hieu pha hoai" +
                    ", chung toi xe dinh chi hoat dong cua ban");
        }
        //set total report cua user
        user.setTotalReports(user.getTotalReports() +1);
        userRepository.save(user);

        //tao report
        Report report = new Report();
        report.setInputType(request.inputType());
        report.setNormalizedValue(normalizedInput);
        report.setReporterId(user.getId());
        report.setStatus(ReportStatus.PENDING);
        report.setReporterStatus(reporterStatus);
        report.setReason(request.reason());
        Report savedReport = reportRepository.save(report);

        return new ReportResponse(
                savedReport.getId(),
                savedReport.getReporterId(),
                savedReport.getInputType(),
                savedReport.getStatus(),
                savedReport.getReason(),
                null,
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
                        null,
                        r.getCreatedAt(),
                        r.getUpdatedAt()
                )).toList();

    }

    @Transactional
    public void cancelReport(Long reportId){
        Report deleteReport = reportRepository.findById(reportId).orElseThrow(()-> new RuntimeException("Report not found"));

        if(deleteReport.getStatus() != ReportStatus.PENDING){
            throw new RuntimeException("only pending report can be delete");
        }

        reportRepository.deleteById(reportId);


    }

}
