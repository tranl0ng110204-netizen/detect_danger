package com.example.detectdanger.controller;

import com.example.detectdanger.dto.report.ReportResponse;
import com.example.detectdanger.entity.ReportStatus;
import com.example.detectdanger.repository.ReportRepository;
import com.example.detectdanger.service.ModerateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public ResponseEntity<ReportResponse> checkReport(@PathVariable Long id) {
        ReportResponse response = moderateService.checkReport(id);
        return ResponseEntity.ok(response);
    }
}
