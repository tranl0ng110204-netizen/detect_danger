package com.example.detectdanger.controller;

import com.example.detectdanger.dto.audit.AuditResponse;
import com.example.detectdanger.dto.moderator.ModeratorDecisionResponse;
import com.example.detectdanger.dto.report.ReportResponse;
import com.example.detectdanger.entity.ReportStatus;
import com.example.detectdanger.repository.ReportRepository;
import com.example.detectdanger.service.ModerateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/moderator")
@RequiredArgsConstructor
public class ModerationController {
    private final ModerateService moderateService;

    @GetMapping("/reports/pending")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<List<ReportResponse>> getPendingReports(){
        try{
            return ResponseEntity.ok(moderateService.getPendingReport());
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }

    }

    @PatchMapping("/reports/{id}/review")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<ReportResponse> checkReport(@PathVariable Long id,Authentication authentication) {
        ReportResponse response = moderateService.checkReport(id,authentication);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/reports/{id}/verify")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<ReportResponse> verifyReport(@PathVariable Long id,
                                                       @Valid @RequestBody ModeratorDecisionResponse response,
                                                       Authentication authentication){
        ReportResponse verifyResponse = moderateService.verifyReport(id,response,authentication);
        return ResponseEntity.ok(verifyResponse);
    }

    @PatchMapping("/reports/{id}/reject")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<ReportResponse> rejectReport(@PathVariable Long id,
                                                       @Valid @RequestBody ModeratorDecisionResponse response,
                                                       Authentication authentication){
        ReportResponse rejectResponse = moderateService.rejectReport(id,response,authentication);
        return ResponseEntity.ok(rejectResponse);
    }

    @GetMapping("/reports/{id}/audit")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<List<AuditResponse>> getReportAudit(@PathVariable Long id){
        return ResponseEntity.ok(moderateService.getReportAudit(id));
    }
}
