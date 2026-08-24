package com.example.detectdanger.service;

import com.example.detectdanger.dto.report.ReportResponse;
import com.example.detectdanger.entity.Report;
import com.example.detectdanger.entity.ReportStatus;
import com.example.detectdanger.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ModerateService {
    private final ReportRepository reportRepository;

    @Transactional(readOnly = true)
    public List<ReportResponse> getPendingReport(){
        return reportRepository.findByStatus(ReportStatus.PENDING)
                .stream()
                .map(r->new ReportResponse(
                        r.getId(),
                        r.getReporterId(),
                        r.getInputType(),
                        r.getStatus(),
                        r.getReason(),
                        r.getCreatedAt(),
                        r.getUpdatedAt()
                )).toList();
    }

    @Transactional
    public ReportResponse checkReport(Long id){
        Report selectedReport = reportRepository.findById(id).orElseThrow(()-> new RuntimeException("report not found"));
        if(selectedReport.getStatus() != ReportStatus.PENDING){
            throw new RuntimeException("only Pending reports appear");
        }
        selectedReport.setStatus(ReportStatus.REVIEWING);

        Report saved = reportRepository.save(selectedReport);

        return new ReportResponse(
                saved.getId(),
                saved.getReporterId(),
                saved.getInputType(),
                saved.getStatus(),
                saved.getReason(),
                saved.getCreatedAt(),
                saved.getUpdatedAt()
        );
    }
}
