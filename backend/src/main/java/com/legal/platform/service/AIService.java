package com.legal.platform.service;

import com.legal.platform.dto.AIChatResponse;
import com.legal.platform.model.AIChatHistory;
import java.util.List;

public interface AIService {
    AIChatResponse processChat(Long userId, String message);
    List<AIChatHistory> getChatHistory(Long userId);
    String summarizeDocument(String documentText);
}
