package com.legal.platform.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIChatResponse {
    private String responseText;
    private String category;
    private List<String> checklists;
    private List<String> requiredDocuments;
    private List<String> explainedTerms;
    private List<LawyerDTO> recommendedLawyers;
}
