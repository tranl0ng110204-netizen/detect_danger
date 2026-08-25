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
import com.example.detectdanger.rule.review.ReportReviewResult;
import com.example.detectdanger.rule.review.review_rules.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ModerateService {
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final AuditRepository auditRepository;
    private final ReporterReputationService reporterReputationService;
    private final BlackListRepository blackListRepository;
    private final VerifyDataRepository verifyDataRepository;
    private final ReportReviewEngine reportReviewEngine;

    @Transactional(readOnly = true)
    public List<ReportResponse> getCheckingReport(){
        return reportRepository.findByStatusIn(
                        Arrays.asList(ReportStatus.PENDING,ReportStatus.REVIEWING))
                .stream()
                .map(r->new ReportResponse(
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
    public Report getReportDetail(Long id){
        return reportRepository.findById(id).orElseThrow(()->new RuntimeException("report not found"));

    }

    @Transactional
    public ReportResponse checkReport(Long id,Authentication authentication){
        //tim report
        Report selectedReport = reportRepository.findById(id).orElseThrow(()-> new RuntimeException("report not found"));

        //check report co dang o trang thai PENDING Khong
        if(selectedReport.getStatus() != ReportStatus.PENDING){
            throw new RuntimeException("only Pending reports appear");
        }


        //goi user Moderator
        User moderator = userRepository.findByEmail(authentication.getName())
                .orElseThrow(()-> new RuntimeException("user not found"));

        //chuyen PENDING-> REVIEWIMG
        selectedReport.setStatus(ReportStatus.REVIEWING);
        Report saved = reportRepository.save(selectedReport);

        //check report review result
        User reporter = userRepository.findById(saved.getReporterId()).orElseThrow(()-> new RuntimeException("user not found"));
        ReportReviewContext context = new ReportReviewContext(
                saved,
                reporter,
                reporter.getReputationScore(),
                reportRepository.
                        existsByNormalizedValueAndStatus(saved.getNormalizedValue(),saved.getStatus()),
                verifyDataRepository.
                        existsByInputTypeAndNormalizedValue(saved.getInputType(),saved.getNormalizedValue()),
                blackListRepository.
                        existsByInputTypeAndNormalizedValueAndActiveTrue(saved.getInputType(),saved.getNormalizedValue())


        );
        ReportReviewResult reportReviewResult = reportReviewEngine.review(context);


        //tao Audit
        Audit audit = new Audit();
        audit.setReportId(id);
        audit.setModeratorId(moderator.getId());
        audit.setAuditAction(AuditAction.START_REVIEW);
        audit.setReason("Start review the report by moderator");
        auditRepository.save(audit);

        //tra ve report response
        return new ReportResponse(
                saved.getId(),
                saved.getReporterId(),
                saved.getInputType(),
                saved.getStatus(),
                saved.getReason(),
                reportReviewResult,
                saved.getCreatedAt(),
                saved.getUpdatedAt()
        );
    }

    @Transactional
    public ReportResponse verifyReport(Long id, ModeratorDecisionResponse response, Authentication authentication){
        //tim report
        Report selectReport = reportRepository.findById(id).orElseThrow(()->new RuntimeException("report not found"));

        //check report co trong trang thai REVIEWING khong?
        if(selectReport.getStatus() != ReportStatus.REVIEWING){
            throw new RuntimeException("only reviewing ports are appear");
        }

        //lay thong tin Moderator
        String email = authentication.getName();
        User moderator = userRepository.findByEmail(email).orElseThrow(()->new RuntimeException("user khong duoc verify"));

        //check xem report da co trong database chua
        boolean alreadyVerified = verifyDataRepository.existsByInputTypeAndNormalizedValue(selectReport.getInputType(),selectReport.getNormalizedValue());

        if(alreadyVerified){
            throw new RuntimeException("report already have in database");
        }

        //chuyen sang REVIEWING -> VERIFY
        selectReport.setStatus(ReportStatus.VERIFIED);
        Report savedReport = reportRepository.save(selectReport);

        //lay user va set reputation score
        //check report review result
        User reporter = userRepository.findById(savedReport.getReporterId()).orElseThrow(()-> new RuntimeException("user not found"));
        ReportReviewContext context = new ReportReviewContext(
                savedReport,
                reporter,
                reporter.getReputationScore(),
                reportRepository.
                        existsByNormalizedValueAndStatus(savedReport.getNormalizedValue(),savedReport.getStatus()),
                verifyDataRepository.
                        existsByInputTypeAndNormalizedValue(savedReport.getInputType(),savedReport.getNormalizedValue()),
                blackListRepository.
                        existsByInputTypeAndNormalizedValueAndActiveTrue(savedReport.getInputType(),savedReport.getNormalizedValue())


        );
        ReportReviewResult reportReviewResult = reportReviewEngine.review(context);

        //tao VerifyData
        VerifiedData verifiedData = new VerifiedData();
        verifiedData.setInputType(savedReport.getInputType());
        verifiedData.setNormalizedValue(savedReport.getNormalizedValue());
        verifiedData.setReportId(id);
        verifiedData.setVerifiedBy(moderator.getId());
        verifyDataRepository.save(verifiedData);





        //tao audit
        Audit audit = new Audit();
        audit.setReportId(savedReport.getId());
        audit.setModeratorId(moderator.getId());
        audit.setAuditAction(AuditAction.VERIFY);
        audit.setReason(response.reason());
        auditRepository.save(audit);


        return new ReportResponse(
                savedReport.getId(),
                savedReport.getReporterId(),
                savedReport.getInputType(),
                savedReport.getStatus(),
                savedReport.getReason(),
                reportReviewResult,
                savedReport.getCreatedAt(),
                savedReport.getUpdatedAt()
        );

   }

    @Transactional
    public ReportResponse rejectReport(Long id, ModeratorDecisionResponse response, Authentication authentication){
        //tim report
        Report selectReport = reportRepository.findById(id).orElseThrow(()->new RuntimeException("report not found"));

        //check report co trong trang thai REVIEWING khong?
        if(selectReport.getStatus() != ReportStatus.REVIEWING){
            throw new RuntimeException("only reviewing ports are appear");
        }

        //lay thong tin Moderator
        String email = authentication.getName();
        User moderator = userRepository.findByEmail(email).orElseThrow(()->new RuntimeException("user not found"));

        //chuyen tu REVIEW -> REJECT
        selectReport.setStatus(ReportStatus.REJECTED);
        Report savedReport = reportRepository.save(selectReport);

        //lay user va set reputation score
        //check report review result
        User reporter = userRepository.findById(savedReport.getReporterId()).orElseThrow(()-> new RuntimeException("user not found"));
        ReportReviewContext context = new ReportReviewContext(
                savedReport,
                reporter,
                reporter.getReputationScore(),
                reportRepository.
                        existsByNormalizedValueAndStatus(savedReport.getNormalizedValue(),savedReport.getStatus()),
                verifyDataRepository.
                        existsByInputTypeAndNormalizedValue(savedReport.getInputType(),savedReport.getNormalizedValue()),
                blackListRepository.
                        existsByInputTypeAndNormalizedValueAndActiveTrue(savedReport.getInputType(),savedReport.getNormalizedValue())


        );
        ReportReviewResult reportReviewResult = reportReviewEngine.review(context);



        //tao audit
        Audit audit = new Audit();
        audit.setReportId(savedReport.getId());
        audit.setModeratorId(moderator.getId());
        audit.setAuditAction(AuditAction.REJECT);
        audit.setReason(response.reason());

        auditRepository.save(audit);


        return new ReportResponse(
                savedReport.getId(),
                savedReport.getReporterId(),
                savedReport.getInputType(),
                savedReport.getStatus(),
                savedReport.getReason(),
                reportReviewResult,
                savedReport.getCreatedAt(),
                savedReport.getUpdatedAt()
        );

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
