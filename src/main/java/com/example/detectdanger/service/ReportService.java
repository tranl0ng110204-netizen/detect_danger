package com.example.detectdanger.service;

import com.example.detectdanger.dto.report.ReportRequest;
import com.example.detectdanger.dto.report.ReportResponse;
import com.example.detectdanger.entity.Enum.ReporterStatus;
import com.example.detectdanger.entity.Report;
import com.example.detectdanger.entity.Enum.ReportStatus;
import com.example.detectdanger.entity.ReportReviewResult;
import com.example.detectdanger.entity.User;
import com.example.detectdanger.repository.ReportRepository;
import com.example.detectdanger.repository.ReportReviewResultRepository;
import com.example.detectdanger.repository.UserRepository;
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
    private final ReportReviewResultRepository reportReviewResultRepository;


    @Transactional
    public ReportResponse createReport(ReportRequest request, Authentication authentication){
        // lay email user trong Jwt
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("user not found"));

        String validatedInput = validationService.validate(request.inputType(),request.content());
        String normalizedInput = normalizeService.normalize(request.inputType(),request.content());

        boolean alreadyReportByUser = reportRepository.existsByReporterIdAndNormalizedValue(user.getId(),normalizedInput);
        if(alreadyReportByUser){
            throw new IllegalArgumentException("Bạn đã gửi báo cáo cho nội dung này trước đó rồi. Đang chờ xử lý!");
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
        report.setReporter(user);
        report.setStatus(ReportStatus.PENDING);
        report.setReporterStatus(reporterStatus);
        report.setReason(request.reason());
        Report savedReport = reportRepository.save(report);
        reportRepository.flush();

        return new ReportResponse(
                savedReport.getId(),
                savedReport.getReporter().getId(),
                savedReport.getInputType(),
                savedReport.getNormalizedValue(),
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
                        r.getReporter().getId(),
                        r.getInputType(),
                        r.getNormalizedValue(),
                        r.getStatus(),
                        r.getReason(),
                        null,
                        r.getCreatedAt(),
                        r.getUpdatedAt()
                )).toList();

    }

    public ReportResponse getReportDetail(Long id){
        Report selected = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("report not found"));
        ReportReviewResult result = reportReviewResultRepository.findByReportId(id);

        return new ReportResponse(
                selected.getId(),
                selected.getReporter().getId(),
                selected.getInputType(),
                selected.getNormalizedValue(),
                selected.getStatus(),
                selected.getReason(),
                result,
                selected.getCreatedAt(),
                selected.getUpdatedAt()
        );

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
