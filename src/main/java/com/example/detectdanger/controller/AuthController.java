package com.example.detectdanger.controller;

import com.example.detectdanger.dto.API_Response;
import com.example.detectdanger.dto.auth.LoginRequest;
import com.example.detectdanger.dto.auth.LoginResponse;
import com.example.detectdanger.dto.auth.RegisterRequest;
import com.example.detectdanger.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;


    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request){
        authService.register(request);
        return ResponseEntity.ok().build();

    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request){
        try{
            return ResponseEntity.ok(authService.login(request));
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new API_Response(e.getMessage(),null));
        }

    }

}
