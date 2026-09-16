package com.example.detectdanger.controller;

import com.example.detectdanger.dto.report.ReportRequest;
import com.example.detectdanger.dto.report.ReportResponse;
import com.example.detectdanger.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @PostMapping("/create")
    public ResponseEntity<ReportResponse> createReport(@Valid @RequestBody ReportRequest request, Authentication authentication){
        ReportResponse response = reportService.createReport(request,authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<ReportResponse>> userReports(Authentication authentication){
        List<ReportResponse> response = reportService.userReports(authentication);

        return ResponseEntity.ok().body(response);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    @GetMapping("/detail/{id}")
    public ResponseEntity<ReportResponse> getReportDetail(@PathVariable Long id, Authentication authentication){
        return ResponseEntity.ok(reportService.getReportDetail(id, authentication));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteReport(@PathVariable Long id, Authentication authentication){
        reportService.cancelReport(id, authentication);
        return ResponseEntity.noContent().build();
    }
}
