package com.example.detectdanger.controller;

import com.example.detectdanger.dto.page.PageResponse;
import com.example.detectdanger.dto.scan.ScanResponse;
import com.example.detectdanger.service.ScanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class HistoryController {
    private final ScanService scanService;

    @GetMapping
    public ResponseEntity<PageResponse<ScanResponse>> getPaginationHistory(
            Authentication authentication,
            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size){
        return ResponseEntity.ok(scanService.getHistoryScanWithPanigation(authentication,page,size));
    }
}
