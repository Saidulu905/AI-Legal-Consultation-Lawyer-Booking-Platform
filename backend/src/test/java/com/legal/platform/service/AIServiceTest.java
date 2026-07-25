package com.legal.platform.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.legal.platform.dto.AIChatResponse;
import com.legal.platform.model.*;
import com.legal.platform.repository.*;
import com.legal.platform.service.impl.AIServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AIServiceTest {

    @Mock
    private AIChatHistoryRepository aiChatHistoryRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private LawyerProfileRepository lawyerProfileRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AIServiceImpl aiService;

    private User clientUser;

    @BeforeEach
    void setUp() {
        clientUser = User.builder().id(1L).name("John Doe").email("client@legalplatform.com").role(Role.CLIENT).build();
        ReflectionTestUtils.setField(aiService, "geminiApiKey", "");
        ReflectionTestUtils.setField(aiService, "geminiApiUrl", "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent");
    }

    @Test
    void processChat_MockFallback_Success() {
        Category mockCat = Category.builder().id(10L).name("Labor Law").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(clientUser));
        when(categoryRepository.findByName(any())).thenReturn(Optional.of(mockCat));
        when(lawyerProfileRepository.findByCategoryAndIsApproved(any(), eq(true))).thenReturn(new ArrayList<>());

        AIChatResponse response = aiService.processChat(1L, "My landlord is threatening to evict me");

        assertNotNull(response);
        assertNotNull(response.getResponseText());
        assertNotNull(response.getCategory());
        assertNotNull(response.getChecklists());
        verify(aiChatHistoryRepository, times(1)).save(any(AIChatHistory.class));
    }

    @Test
    void summarizeDocument_MockFallback_Success() {
        String docText = "This Lease Agreement is made on Jan 1st 2026 between Landlord Bob and Tenant Alice...";
        String summary = aiService.summarizeDocument(docText);

        assertNotNull(summary);
        assertTrue(summary.contains("Document Summary"));
    }
}
