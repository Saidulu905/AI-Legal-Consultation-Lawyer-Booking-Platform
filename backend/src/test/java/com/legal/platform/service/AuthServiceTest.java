package com.legal.platform.service;

import com.legal.platform.config.JwtService;
import com.legal.platform.dto.*;
import com.legal.platform.model.*;
import com.legal.platform.repository.*;
import com.legal.platform.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private LawyerProfileRepository lawyerProfileRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private AuditLogRepository auditLogRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("Sarah Jenkins")
                .email("lawyer@legalplatform.com")
                .password("encoded_password")
                .role(Role.LAWYER)
                .isVerified(false)
                .verificationCode("code-123")
                .build();

        registerRequest = new RegisterRequest();
        registerRequest.setName("Sarah Jenkins");
        registerRequest.setEmail("lawyer@legalplatform.com");
        registerRequest.setPassword("LawyerPassword123");
        registerRequest.setRole(Role.LAWYER);
    }

    @Test
    void register_Success() {
        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserDTO result = authService.register(registerRequest);

        assertNotNull(result);
        assertEquals(testUser.getEmail(), result.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
        verify(lawyerProfileRepository, times(1)).save(any(LawyerProfile.class));
    }

    @Test
    void register_EmailAlreadyExists_ThrowsException() {
        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.of(testUser));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("Email already in use", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_Success() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("lawyer@legalplatform.com");
        loginRequest.setPassword("LawyerPassword123");

        testUser.setVerified(true);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(true);
        when(jwtService.generateToken(any())).thenReturn("access_token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LoginResponse result = authService.login(loginRequest);

        assertNotNull(result);
        assertEquals("access_token", result.getToken());
        assertNotNull(result.getRefreshToken());
    }

    @Test
    void verifyEmail_Success() {
        when(userRepository.findByVerificationCode("code-123")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        authService.verifyEmail("code-123");

        assertTrue(testUser.isVerified());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void logout_Success() {
        RefreshToken token = new RefreshToken();
        token.setUser(testUser);
        
        when(userRepository.findByEmail("lawyer@legalplatform.com")).thenReturn(Optional.of(testUser));
        
        authService.logout("lawyer@legalplatform.com");

        verify(refreshTokenRepository, times(1)).deleteByUser(testUser);
    }
}
