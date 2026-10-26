package com.engineeringdigest.url_shortener.service;


import com.engineeringdigest.url_shortener.dto.TokenResponse;
import com.engineeringdigest.url_shortener.entity.RefreshToken;
import com.engineeringdigest.url_shortener.entity.User;
import com.engineeringdigest.url_shortener.exception.EmailAlreadyExistsException;
import com.engineeringdigest.url_shortener.exception.InvalidCredentialsException;
import com.engineeringdigest.url_shortener.exception.InvalidRefreshTokenException;
import com.engineeringdigest.url_shortener.repository.RefreshTokenRepository;
import com.engineeringdigest.url_shortener.repository.UserRepository;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final RefreshTokenRepository refreshTokenRepository;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    public User signup(String email, String password) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new EmailAlreadyExistsException("This email is already registered");
        }
    }

    public TokenResponse login(String email, String password) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
        return new TokenResponse(jwtService.generateToken(user.getId()), createRefreshToken(user.getId()).getToken());
    }

    private RefreshToken createRefreshToken(Long userId) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(userId);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiresAt(LocalDateTime.now().plusSeconds(refreshExpiration / 1000));
        return refreshTokenRepository.save(refreshToken);
    }

    public TokenResponse refresh(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidRefreshTokenException("Invalid refresh token"));

        if (refreshToken.isRevoked()) {
            revokeAllTokens(refreshToken.getUserId());
            throw new InvalidRefreshTokenException(
                    "This refresh token was already used. All sessions have been logged out. Please login again.");
        }

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new InvalidRefreshTokenException("Refresh token has expired. Please login again.");
        }
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
        return new TokenResponse(
                jwtService.generateToken(refreshToken.getUserId()),
                createRefreshToken(refreshToken.getUserId()).getToken());
    }

    private void revokeAllTokens(Long userId) {
        List<RefreshToken> tokens = refreshTokenRepository.findAllByUserId(userId);
        tokens.forEach(t -> t.setRevoked(true));
        refreshTokenRepository.saveAll(tokens);
    }

}
