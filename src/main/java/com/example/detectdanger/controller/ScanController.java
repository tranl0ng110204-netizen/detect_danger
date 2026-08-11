package com.example.detectdanger.controller;

import com.example.detectdanger.dto.scan.ScanRequest;
import com.example.detectdanger.dto.scan.ScanResponse;
import com.example.detectdanger.service.ScanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scan")
@RequiredArgsConstructor
public class ScanController {
    private final ScanService scanService;

    @PostMapping("/create")
    public ResponseEntity<ScanResponse> createReport(@Valid @RequestBody ScanRequest request, Authentication authentication){
        return ResponseEntity.ok(
                scanService.createScan(request, authentication)
        );
    }

    @GetMapping("/history")
    public ResponseEntity<List<ScanResponse>> getScanHistory(Authentication authentication){
        return ResponseEntity.ok(
                scanService.getHistoryScan(authentication)
        );
    }

}
