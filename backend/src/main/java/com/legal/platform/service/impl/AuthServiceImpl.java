package com.legal.platform.service.impl;

import com.legal.platform.config.JwtService;
import com.legal.platform.dto.*;
import com.legal.platform.model.*;
import com.legal.platform.repository.*;
import com.legal.platform.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LawyerProfileRepository lawyerProfileRepository;
    private final CategoryRepository categoryRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    @Override
    @Transactional
    public UserDTO register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already in use");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .isVerified(true)
                .verificationCode(null)
                .build();

        User savedUser = userRepository.save(user);

        // If the user is a lawyer, create the lawyer profile
        if (request.getRole() == Role.LAWYER) {
            Category category = null;
            if (request.getCategoryId() != null) {
                category = categoryRepository.findById(request.getCategoryId())
                        .orElseThrow(() -> new RuntimeException("Category not found"));
            }

            LawyerProfile lawyerProfile = LawyerProfile.builder()
                    .user(savedUser)
                    .specialization(request.getSpecialization())
                    .bio(request.getBio())
                    .experienceYears(request.getExperienceYears() != null ? request.getExperienceYears() : 0)
                    .hourlyRate(request.getHourlyRate() != null ? request.getHourlyRate() : 0.0)
                    .category(category)
                    .isApproved(false) // Needs Admin Approval
                    .build();
            lawyerProfileRepository.save(lawyerProfile);
        }

        // Write Audit Log
        auditLogRepository.save(AuditLog.builder()
                .action("USER_REGISTER")
                .performedBy(savedUser.getEmail())
                .details("Registered user as " + request.getRole())
                .build());

        // MOCK EMAIL PRINT
        System.out.println("==================================================");
        System.out.println("AUTO-VERIFIED ACCOUNT FOR: " + savedUser.getEmail());
        System.out.println("==================================================");

        return convertToUserDTO(savedUser);
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        // Auto-verified: skipping email verification check

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );

        String jwt = jwtService.generateToken(userDetails);

        // Manage refresh token
        refreshTokenRepository.deleteByUser(user);
        refreshTokenRepository.flush();
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshExpirationMs))
                .build();
        refreshTokenRepository.save(refreshToken);

        // Write Audit Log
        auditLogRepository.save(AuditLog.builder()
                .action("USER_LOGIN")
                .performedBy(user.getEmail())
                .details("Logged in successfully")
                .build());

        return LoginResponse.builder()
                .token(jwt)
                .refreshToken(refreshToken.getToken())
                .user(convertToUserDTO(user))
                .build();
    }

    @Override
    @Transactional
    public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
        String token = request.getRefreshToken();
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh token is not in database!"));

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("Refresh token was expired. Please make a new signin request");
        }

        User user = refreshToken.getUser();
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );

        String newJwt = jwtService.generateToken(userDetails);
        
        // Rotate refresh token
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshExpirationMs));
        refreshTokenRepository.save(refreshToken);

        return TokenRefreshResponse.builder()
                .token(newJwt)
                .refreshToken(refreshToken.getToken())
                .build();
    }

    @Override
    @Transactional
    public void logout(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        refreshTokenRepository.deleteByUser(user);
        
        // Write Audit Log
        auditLogRepository.save(AuditLog.builder()
                .action("USER_LOGOUT")
                .performedBy(email)
                .details("Logged out successfully")
                .build());
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String resetCode = UUID.randomUUID().toString().substring(0, 8);
        user.setResetPasswordCode(resetCode);
        userRepository.save(user);

        // MOCK EMAIL PRINT
        System.out.println("==================================================");
        System.out.println("MOCK EMAIL TO: " + user.getEmail());
        System.out.println("SUBJECT: Reset Your Password");
        System.out.println("BODY: Reset password link code: " + resetCode);
        System.out.println("==================================================");
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByResetPasswordCode(request.getCode())
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset code"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetPasswordCode(null);
        userRepository.save(user);

        // Write Audit Log
        auditLogRepository.save(AuditLog.builder()
                .action("PASSWORD_RESET")
                .performedBy(user.getEmail())
                .details("Password reset successfully")
                .build());
    }

    @Override
    @Transactional
    public void verifyEmail(String code) {
        User user = userRepository.findByVerificationCode(code)
                .orElseThrow(() -> new RuntimeException("Invalid verification code"));

        user.setVerified(true);
        user.setVerificationCode(null);
        userRepository.save(user);

        // Write Audit Log
        auditLogRepository.save(AuditLog.builder()
                .action("EMAIL_VERIFICATION")
                .performedBy(user.getEmail())
                .details("Email verified successfully")
                .build());
    }

    private UserDTO convertToUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .isVerified(user.isVerified())
                .build();
    }
}
