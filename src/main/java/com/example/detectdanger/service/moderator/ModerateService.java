package com.example.detectdanger.service.moderator;

import com.example.detectdanger.dto.audit.AuditResponse;
import com.example.detectdanger.dto.moderator.ModeratorDecisionResponse;
import com.example.detectdanger.dto.report.ReportResponse;
import com.example.detectdanger.entity.*;
import com.example.detectdanger.entity.Enum.AuditAction;
import com.example.detectdanger.entity.Enum.ReportStatus;
import com.example.detectdanger.repository.*;
import com.example.detectdanger.rule.review.ReportReviewContext;
import com.example.detectdanger.rule.review.ReportReviewEngine;
import com.example.detectdanger.rule.review.ReportCheckingResult;
import com.example.detectdanger.rule.review.review_rules.ReviewResult;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import com.example.detectdanger.dto.moderator.ModeratorDecisionRequest;
import com.example.detectdanger.exceptions.BusinessException;
import com.example.detectdanger.exceptions.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class ModerateService {
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final AuditRepository auditRepository;
    private final ReportReviewResultRepository reportReviewResultRepository;
    private final BlackListRepository blackListRepository;
    private final VerifyDataRepository verifyDataRepository;
    private final ReportReviewEngine reportReviewEngine;
    private final ReporterReputationService reporterReputationService;

    @Transactional(readOnly = true)
    public List<ReportResponse> getCheckingReport(){
        return reportRepository.findByStatusIn(
                        Arrays.asList(ReportStatus.PENDING,ReportStatus.REVIEWING))
                .stream()
                .map(r->new ReportResponse(
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


    private ReportReviewContext createContext(Report report){
        User reporter = userRepository.findById(report.getReporter().getId())
                .orElseThrow(()-> new ResourceNotFoundException("Reporter user not found"));
        return new ReportReviewContext(
                report,
                reporter,
                reporter.getReputationScore(),
                reportRepository.existsByNormalizedValueAndStatus(
                        report.getNormalizedValue(), report.getStatus()),
                verifyDataRepository.
                        existsByInputTypeAndNormalizedValue(report.getInputType(),report.getNormalizedValue()),
                blackListRepository.
                        existsByInputTypeAndNormalizedValueAndActiveTrue(report.getInputType(),report.getNormalizedValue())
        );
    }



    @Transactional
    public ReportResponse getReportDetail(Long id){
        Report saved = reportRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Report not found with id: " + id));

        ReportReviewResult reportReviewResult = reportReviewResultRepository.findByReportId(id);
        if (reportReviewResult == null) {
            ReportReviewContext context = createContext(saved);
            ReportCheckingResult reportCheckingResult = reportReviewEngine.review(context);
            List<String> rulesChecking = reportCheckingResult.getRuleResults().stream()
                    .map(ReviewResult::getReason)
                    .toList();
            reportReviewResult = ReportReviewResult.builder()
                    .report(saved)
                    .totalScore(reportCheckingResult.getTotalScore())
                    .recommendation(reportCheckingResult.getRecommendation())
                    .ruleResult(rulesChecking)
                    .build();
            reportReviewResult = reportReviewResultRepository.save(reportReviewResult);
        }

        return new ReportResponse(
                saved.getId(),
                saved.getReporter().getId(),
                saved.getInputType(),
                saved.getNormalizedValue(),
                saved.getStatus(),
                saved.getReason(),
                reportReviewResult,
                saved.getCreatedAt(),
                saved.getUpdatedAt()
        );
    }

    @Transactional
    public ReportResponse checkReport(Long id, Authentication authentication){
        Report selectedReport = reportRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Report not found with id: " + id));

        if(selectedReport.getStatus() != ReportStatus.PENDING){
            throw new BusinessException("Chỉ có thể chuyển báo cáo PENDING sang REVIEWING");
        }

        User moderator = userRepository.findByEmail(authentication.getName())
                .orElseThrow(()-> new ResourceNotFoundException("Moderator not found: " + authentication.getName()));

        selectedReport.setStatus(ReportStatus.REVIEWING);
        Report saved = reportRepository.save(selectedReport);

        ReportReviewContext context = createContext(saved);
        ReportCheckingResult reportCheckingResult = reportReviewEngine.review(context);
        List<String> rulesChecking = reportCheckingResult.getRuleResults().stream()
                .map(ReviewResult::getReason)
                .toList();

        ReportReviewResult reportReviewResult = reportReviewResultRepository.findByReportId(id);
        if (reportReviewResult == null) {
            reportReviewResult = ReportReviewResult.builder()
                    .report(saved)
                    .totalScore(reportCheckingResult.getTotalScore())
                    .recommendation(reportCheckingResult.getRecommendation())
                    .ruleResult(rulesChecking)
                    .build();
        } else {
            reportReviewResult.setTotalScore(reportCheckingResult.getTotalScore());
            reportReviewResult.setRecommendation(reportCheckingResult.getRecommendation());
            reportReviewResult.setRuleResult(rulesChecking);
        }
        reportReviewResult = reportReviewResultRepository.save(reportReviewResult);

        Audit audit = new Audit();
        audit.setReportId(id);
        audit.setModeratorId(moderator.getId());
        audit.setAuditAction(AuditAction.START_REVIEW);
        audit.setReason("Start review the report by moderator");
        auditRepository.save(audit);

        return new ReportResponse(
                saved.getId(),
                saved.getReporter().getId(),
                saved.getInputType(),
                saved.getNormalizedValue(),
                saved.getStatus(),
                saved.getReason(),
                reportReviewResult,
                saved.getCreatedAt(),
                saved.getUpdatedAt()
        );
    }

    @Transactional
    public ReportResponse verifyReport(Long id, ModeratorDecisionRequest request, Authentication authentication){
        Report selectReport = reportRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Report not found with id: " + id));
        ReportReviewResult reportReviewResult = reportReviewResultRepository.findByReportId(id);

        if(selectReport.getStatus() != ReportStatus.REVIEWING){
            throw new BusinessException("Chỉ có thể verify báo cáo đang trong trạng thái REVIEWING");
        }

        String email = authentication.getName();
        User moderator = userRepository.findByEmail(email)
                .orElseThrow(()->new ResourceNotFoundException("Moderator not found: " + email));

        selectReport.setStatus(ReportStatus.VERIFIED);
        Report savedReport = reportRepository.save(selectReport);

        VerifiedData verifiedData = new VerifiedData();
        verifiedData.setInputType(savedReport.getInputType());
        verifiedData.setNormalizedValue(savedReport.getNormalizedValue());
        verifiedData.setReportId(id);
        verifiedData.setVerifiedBy(moderator.getId());
        verifyDataRepository.save(verifiedData);
        verifyDataRepository.flush();

        User reporter = userRepository.findById(savedReport.getReporter().getId())
                .orElseThrow(()-> new ResourceNotFoundException("Reporter not found"));
        reporterReputationService.handleVerifyReport(reporter);

        Audit audit = new Audit();
        audit.setReportId(savedReport.getId());
        audit.setModeratorId(moderator.getId());
        audit.setAuditAction(AuditAction.VERIFY);
        audit.setReason(request.reason());
        auditRepository.save(audit);

        return new ReportResponse(
                savedReport.getId(),
                savedReport.getReporter().getId(),
                savedReport.getInputType(),
                savedReport.getNormalizedValue(),
                savedReport.getStatus(),
                savedReport.getReason(),
                reportReviewResult,
                savedReport.getCreatedAt(),
                savedReport.getUpdatedAt()
        );
    }

    @Transactional
    public ReportResponse verifyReport(Long id, ModeratorDecisionResponse response, Authentication authentication){
        return verifyReport(id, new ModeratorDecisionRequest(response.reason()), authentication);
    }

    @Transactional
    public ReportResponse rejectReport(Long id, ModeratorDecisionRequest request, Authentication authentication){
        Report selectReport = reportRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Report not found with id: " + id));
        ReportReviewResult reportReviewResult = reportReviewResultRepository.findByReportId(id);

        if(selectReport.getStatus() != ReportStatus.REVIEWING){
            throw new BusinessException("Chỉ có thể reject báo cáo đang trong trạng thái REVIEWING");
        }

        String email = authentication.getName();
        User moderator = userRepository.findByEmail(email)
                .orElseThrow(()->new ResourceNotFoundException("Moderator not found: " + email));

        selectReport.setStatus(ReportStatus.REJECTED);
        Report savedReport = reportRepository.save(selectReport);

        User reporter = userRepository.findById(savedReport.getReporter().getId())
                .orElseThrow(()-> new ResourceNotFoundException("Reporter not found"));
        reporterReputationService.handleRejected(reporter);

        Audit audit = new Audit();
        audit.setReportId(savedReport.getId());
        audit.setModeratorId(moderator.getId());
        audit.setAuditAction(AuditAction.REJECT);
        audit.setReason(request.reason());
        auditRepository.save(audit);

        return new ReportResponse(
                savedReport.getId(),
                savedReport.getReporter().getId(),
                savedReport.getInputType(),
                savedReport.getNormalizedValue(),
                savedReport.getStatus(),
                savedReport.getReason(),
                reportReviewResult,
                savedReport.getCreatedAt(),
                savedReport.getUpdatedAt()
        );
    }

    @Transactional
    public ReportResponse rejectReport(Long id, ModeratorDecisionResponse response, Authentication authentication){
        return rejectReport(id, new ModeratorDecisionRequest(response.reason()), authentication);
    }

    @Transactional(readOnly = true)
    public List<AuditResponse> getReportAudit(Long reportId){
        if(!reportRepository.existsById(reportId)){
            throw new RuntimeException("No report is found");
        }
        return auditRepository.findByReportIdOrderByCreatedAtAsc(reportId)
                .stream()
                .map(audit -> new AuditResponse(
                        audit.getId(),
                        audit.getReportId(),
                        audit.getModeratorId(),
                        audit.getAuditAction(),
                        audit.getReason(),
                        audit.getCreatedAt()
                ))
                .toList();
    }
}
