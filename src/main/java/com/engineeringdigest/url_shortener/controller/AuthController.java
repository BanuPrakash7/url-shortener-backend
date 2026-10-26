package com.engineeringdigest.url_shortener.controller;

import com.engineeringdigest.url_shortener.dto.*;
import com.engineeringdigest.url_shortener.entity.RefreshToken;
import com.engineeringdigest.url_shortener.entity.User;

import com.engineeringdigest.url_shortener.repository.RefreshTokenRepository;
import com.engineeringdigest.url_shortener.service.AuthService;
import com.engineeringdigest.url_shortener.service.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        User user = authService.signup(request.email(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SignupResponse(user.getId(), user.getEmail()));
    }


    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request.email(), request.password());
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request.refreshToken());
    }


}