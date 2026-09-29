package com.fakecompanydetector.service.authentication;

import com.fakecompanydetector.dto.AuthResponse;
import com.fakecompanydetector.dto.LoginRequest;
import com.fakecompanydetector.dto.RegisterRequest;
import com.fakecompanydetector.entity.RefreshToken;
import com.fakecompanydetector.entity.User;
import com.fakecompanydetector.entity.enums.Role;
import com.fakecompanydetector.exception.DuplicateResourceException;
import com.fakecompanydetector.exception.UnauthorizedException;
import com.fakecompanydetector.repository.RefreshTokenRepository;
import com.fakecompanydetector.repository.UserRepository;
import com.fakecompanydetector.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered");
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .isSuspended(false)
                .build();
        
        userRepository.save(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request, String generatedRefreshToken) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        if (user.getIsSuspended()) {
            throw new UnauthorizedException("Account is suspended");
        }

        String accessToken = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        
        RefreshToken rt = RefreshToken.builder()
                .user(user)
                .tokenHash(generatedRefreshToken)
                .expiresAt(LocalDateTime.now().plusNanos(refreshTokenExpiration * 1_000_000))
                .build();
        refreshTokenRepository.save(rt);

        return buildAuthResponse(user, accessToken);
    }

    @Transactional
    public AuthResponse refresh(String refreshTokenValue, String newRefreshTokenValue) {
        RefreshToken rt = refreshTokenRepository.findByTokenHash(refreshTokenValue)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
        
        if (rt.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(rt);
            throw new UnauthorizedException("Refresh token expired");
        }
        
        User user = rt.getUser();
        if (user.getIsSuspended()) {
            throw new UnauthorizedException("Account is suspended");
        }

        refreshTokenRepository.delete(rt);
        
        String accessToken = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        
        RefreshToken newRt = RefreshToken.builder()
                .user(user)
                .tokenHash(newRefreshTokenValue)
                .expiresAt(LocalDateTime.now().plusNanos(refreshTokenExpiration * 1_000_000))
                .build();
        refreshTokenRepository.save(newRt);
        
        return buildAuthResponse(user, accessToken);
    }

    @Transactional
    public void logout(String refreshTokenValue) {
        if (refreshTokenValue != null) {
            refreshTokenRepository.findByTokenHash(refreshTokenValue)
                    .ifPresent(refreshTokenRepository::delete);
        }
    }

    private AuthResponse buildAuthResponse(User user, String accessToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .user(AuthResponse.UserDto.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .role(user.getRole().name())
                        .build())
                .build();
    }
}
