package com.example.detectdanger.controller;

import com.example.detectdanger.dto.audit.AuditResponse;
import com.example.detectdanger.dto.moderator.ModeratorDecisionResponse;
import com.example.detectdanger.dto.report.ReportResponse;
import com.example.detectdanger.entity.Report;
import com.example.detectdanger.service.moderator.ModerateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.example.detectdanger.dto.moderator.ModeratorDecisionRequest;

@RestController
@RequestMapping("/api/moderator")
@RequiredArgsConstructor
public class ModerationController {
    private final ModerateService moderateService;

    @GetMapping("/reports/checking")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<List<ReportResponse>> getCheckingReports(){
        return ResponseEntity.ok(moderateService.getCheckingReport());
    }

    @GetMapping("/reports/{id}")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<ReportResponse> getReportDetail(@PathVariable Long id){
        return ResponseEntity.ok(moderateService.getReportDetail(id));
    }

    @PatchMapping("/reports/{id}/review")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<ReportResponse> checkReport(@PathVariable Long id, Authentication authentication) {
        ReportResponse response = moderateService.checkReport(id, authentication);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/reports/{id}/verify")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<ReportResponse> verifyReport(@PathVariable Long id,
                                                       @Valid @RequestBody ModeratorDecisionRequest request,
                                                       Authentication authentication){
        ReportResponse verifyResponse = moderateService.verifyReport(id, request, authentication);
        return ResponseEntity.ok(verifyResponse);
    }

    @PatchMapping("/reports/{id}/reject")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<ReportResponse> rejectReport(@PathVariable Long id,
                                                       @Valid @RequestBody ModeratorDecisionRequest request,
                                                       Authentication authentication){
        ReportResponse rejectResponse = moderateService.rejectReport(id, request, authentication);
        return ResponseEntity.ok(rejectResponse);
    }

    @GetMapping("/reports/{id}/audit")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<List<AuditResponse>> getReportAudit(@PathVariable Long id){
        return ResponseEntity.ok(moderateService.getReportAudit(id));
    }
}
