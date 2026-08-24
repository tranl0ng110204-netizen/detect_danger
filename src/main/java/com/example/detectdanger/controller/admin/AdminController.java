package com.example.detectdanger.controller.admin;

import com.example.detectdanger.dto.admin.AdminUserResponse;
import com.example.detectdanger.dto.audit.AuditResponse;
import com.example.detectdanger.dto.page.PageResponse;
import com.example.detectdanger.dto.report.ReportResponse;
import com.example.detectdanger.dto.scan.ScanResponse;
import com.example.detectdanger.service.admin.AdminAuditService;
import com.example.detectdanger.service.admin.AdminReportService;
import com.example.detectdanger.service.admin.AdminScanService;
import com.example.detectdanger.service.admin.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminUserService adminUserService;
    private final AdminScanService adminScanService;
    private final AdminReportService adminReportService;
    private final AdminAuditService adminAuditService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<PageResponse<AdminUserResponse>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){
        return ResponseEntity.ok(adminUserService.getUsers(page,size));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/user")
    public void deleteUser(@RequestParam Long userId){
        adminUserService.deleteUser(userId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/scans")
    public ResponseEntity<PageResponse<ScanResponse>> getScans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){
        return ResponseEntity.ok(adminScanService.getScans(page,size));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/reports")
    public ResponseEntity<PageResponse<ReportResponse>> getReports (
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){
        return ResponseEntity.ok(adminReportService.getReports(page,size));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/audits")
    public ResponseEntity<PageResponse<AuditResponse>> getAudits(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        return ResponseEntity.ok(adminAuditService.getAudits(page,size));
    }



}
