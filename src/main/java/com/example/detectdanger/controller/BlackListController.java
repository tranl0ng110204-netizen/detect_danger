package com.example.detectdanger.controller;

import com.example.detectdanger.dto.blacklist.BlackListRequest;
import com.example.detectdanger.dto.blacklist.BlackListResponse;
import com.example.detectdanger.service.BlackListService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/blacklist")
@RequiredArgsConstructor
public class BlackListController {
    private final BlackListService blackListService;

    @PostMapping()
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<BlackListResponse> create(@Valid @RequestBody BlackListRequest request){
        return ResponseEntity.ok(blackListService.createBlackList(request));
    }

}
