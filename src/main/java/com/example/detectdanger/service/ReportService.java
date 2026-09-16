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

import com.example.detectdanger.exceptions.BusinessException;
import com.example.detectdanger.exceptions.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;

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
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        String validatedInput = validationService.validate(request.inputType(),request.content());
        String normalizedInput = normalizeService.normalize(request.inputType(),request.content());

        boolean alreadyReportByUser = reportRepository.existsByReporterIdAndNormalizedValue(user.getId(),normalizedInput);
        if(alreadyReportByUser){
            throw new BusinessException("Bạn đã gửi báo cáo cho nội dung này trước đó rồi. Đang chờ xử lý!");
        }


        ReporterStatus reporterStatus = reporterRiskService.evaluateUser(user);
        if(reporterStatus == ReporterStatus.RESTRICTED){
            throw new BusinessException("Tài khoản của bạn có dấu hiệu vi phạm, chức năng gửi báo cáo đang bị hạn chế.");
        }
        //set total report cua user
        user.setTotalReports(user.getTotalReports() + 1);
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
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
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

    public ReportResponse getReportDetail(Long id, Authentication authentication){
        Report selected = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + id));

        String currentUserEmail = authentication.getName();
        boolean isOwner = selected.getReporter() != null && selected.getReporter().getEmail().equalsIgnoreCase(currentUserEmail);
        boolean isStaff = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_MODERATOR"));

        if (!isOwner && !isStaff) {
            throw new AccessDeniedException("Bạn không có quyền xem chi tiết báo cáo này");
        }

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
    public void cancelReport(Long reportId, Authentication authentication){
        Report deleteReport = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + reportId));

        String currentUserEmail = authentication.getName();
        boolean isOwner = deleteReport.getReporter() != null && deleteReport.getReporter().getEmail().equalsIgnoreCase(currentUserEmail);
        boolean isStaff = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isOwner && !isStaff) {
            throw new AccessDeniedException("Bạn không có quyền hủy báo cáo này");
        }

        if(deleteReport.getStatus() != ReportStatus.PENDING){
            throw new BusinessException("Chỉ có thể hủy báo cáo ở trạng thái PENDING");
        }

        // Giảm totalReports của user
        User reporter = deleteReport.getReporter();
        if (reporter != null && reporter.getTotalReports() != null && reporter.getTotalReports() > 0) {
            reporter.setTotalReports(reporter.getTotalReports() - 1);
            userRepository.save(reporter);
        }

        reportRepository.delete(deleteReport);
    }
}
