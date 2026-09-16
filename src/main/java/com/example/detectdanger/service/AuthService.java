package com.example.detectdanger.service;

import com.example.detectdanger.dto.auth.LoginRequest;
import com.example.detectdanger.dto.auth.LoginResponse;
import com.example.detectdanger.dto.auth.RegisterRequest;
import com.example.detectdanger.entity.Enum.Role;
import com.example.detectdanger.entity.Enum.UserStatus;
import com.example.detectdanger.entity.User;
import com.example.detectdanger.repository.UserRepository;
import com.example.detectdanger.security.JwtService;
import com.example.detectdanger.exceptions.BusinessException;
import com.example.detectdanger.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;


    //dang ki user
    public void register(RegisterRequest request){
        if(userRepository.existsByEmail(request.getEmail())){
            throw new BusinessException("Email already exists");
        }
        if(userRepository.existsByUsername(request.getUserName())){
            throw new BusinessException("User already exists");
        }
        User user = User.builder()
                .username(request.getUserName())
                .email(request.getEmail())
                .passwordHash(
                        passwordEncoder.encode(request.getPassword())
                )
                .role(Role.USER)
                .userStatus(UserStatus.ACTIVE)
                .build();
        userRepository.save(user);
    }

    //dang nhap user
    public LoginResponse login(LoginRequest request){

        //tao token
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        var userDetails = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getEmail()));

        if(userDetails.isDelete() || userDetails.getUserStatus() == UserStatus.SUSPENDED){
            throw new BusinessException("Tài khoản đã bị đình chỉ hoặc khóa");
        }


        var springUser =
                org.springframework.security.core.userdetails.User
                        .withUsername(userDetails.getEmail())
                        .password(userDetails.getPasswordHash())
                        .roles(userDetails.getRole().name())
                        .build();

        String token =
                jwtService.generateToken(springUser);

        return new LoginResponse(
                token,
                userDetails.getReputationScore(),
                "Bearer"
        );
    }

}
