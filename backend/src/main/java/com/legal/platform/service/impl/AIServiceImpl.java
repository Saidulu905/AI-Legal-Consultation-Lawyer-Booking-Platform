package com.legal.platform.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.legal.platform.dto.AIChatResponse;
import com.legal.platform.dto.LawyerDTO;
import com.legal.platform.model.AIChatHistory;
import com.legal.platform.model.Category;
import com.legal.platform.model.LawyerProfile;
import com.legal.platform.model.User;
import com.legal.platform.repository.AIChatHistoryRepository;
import com.legal.platform.repository.CategoryRepository;
import com.legal.platform.repository.LawyerProfileRepository;
import com.legal.platform.repository.ReviewRepository;
import com.legal.platform.repository.UserRepository;
import com.legal.platform.service.AIService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {

    private final AIChatHistoryRepository aiChatHistoryRepository;
    private final UserRepository userRepository;
    private final LawyerProfileRepository lawyerProfileRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewRepository reviewRepository;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Override
    @Transactional
    public AIChatResponse processChat(Long userId, String message) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        AIChatResponse response;

        // Check if Gemini API key is configured
        if (geminiApiKey == null || geminiApiKey.trim().isEmpty() || geminiApiKey.startsWith("YOUR_")) {
            // FALLBACK MOCK MODE
            response = generateMockResponse(message);
        } else {
            try {
                response = callGeminiAPI(message);
            } catch (Exception e) {
                System.err.println("Gemini API call failed: " + e.getMessage() + ". Falling back to Mock response.");
                response = generateMockResponse(message);
            }
        }

        // Attach matching lawyers from database based on the classification category
        List<LawyerDTO> recommendedLawyers = getRecommendations(response.getCategory());
        response.setRecommendedLawyers(recommendedLawyers);

        // Save history in database
        aiChatHistoryRepository.save(AIChatHistory.builder()
                .user(user)
                .query(message)
                .response(response.getResponseText())
                .build());

        return response;
    }

    @Override
    public List<AIChatHistory> getChatHistory(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return aiChatHistoryRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Override
    public String summarizeDocument(String documentText) {
        if (geminiApiKey == null || geminiApiKey.trim().isEmpty() || geminiApiKey.startsWith("YOUR_")) {
            return generateMockSummary(documentText);
        }

        try {
            String prompt = "You are an expert Legal Document Summary Assistant.\n" +
                    "Analyze the following legal document text and summarize it. Extract:\n" +
                    "1. Document Type (e.g. Non-Disclosure Agreement, Lease Agreement, Employment Contract).\n" +
                    "2. Key Parties Involved.\n" +
                    "3. Key Rights and Core Obligations of each party.\n" +
                    "4. Important deadlines, termination criteria, or penalty details.\n" +
                    "5. Potential Red Flags or points of ambiguity.\n" +
                    "Format the response in clean, beautiful Markdown with headers and bullet points.\n" +
                    "Document text:\n" +
                    documentText;

            return callGeminiPlainText(prompt);
        } catch (Exception e) {
            return generateMockSummary(documentText);
        }
    }

    private AIChatResponse callGeminiAPI(String userQuery) throws Exception {
        String prompt = "You are an expert AI Legal Consultation Assistant.\n" +
                "Analyze the user's issue: \"" + userQuery + "\".\n" +
                "Provide your response in JSON format with the following keys:\n" +
                "1. \"responseText\": A helpful, friendly, and structured legal analysis/counsel directly addressing the query. Keep the tone premium and professional. Add standard legal disclaimer that this is information and not formal legal representation.\n" +
                "2. \"category\": Must select exactly one of: \"Labor Law\", \"Criminal Law\", \"Family Law\", \"Corporate Law\", \"Real Estate Law\", \"Intellectual Property\", or \"General\".\n" +
                "3. \"checklists\": A list of 3-5 specific, actionable next steps for the client.\n" +
                "4. \"requiredDocuments\": A list of legal documents/contracts/proof items they should gather.\n" +
                "5. \"explainedTerms\": A list of brief definitions for legal terms relevant to their issue (e.g. \"Severance Pay: Compensation paid to an employee upon termination\").\n" +
                "Return ONLY valid JSON matching this schema. Do not enclose it in ```json blocks.";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", prompt);

        Map<String, Object> parts = new HashMap<>();
        parts.put("parts", Collections.singletonList(textPart));

        Map<String, Object> contents = new HashMap<>();
        contents.put("contents", Collections.singletonList(parts));

        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("responseMimeType", "application/json");
        contents.put("generationConfig", generationConfig);

        String jsonRequest = objectMapper.writeValueAsString(contents);
        HttpEntity<String> entity = new HttpEntity<>(jsonRequest, headers);

        String url = geminiApiUrl + "?key=" + geminiApiKey;
        String rawResponse = restTemplate.postForObject(url, entity, String.class);

        JsonNode root = objectMapper.readTree(rawResponse);
        String jsonText = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();

        return objectMapper.readValue(jsonText, AIChatResponse.class);
    }

    private String callGeminiPlainText(String prompt) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", prompt);

        Map<String, Object> parts = new HashMap<>();
        parts.put("parts", Collections.singletonList(textPart));

        Map<String, Object> contents = new HashMap<>();
        contents.put("contents", Collections.singletonList(parts));

        String jsonRequest = objectMapper.writeValueAsString(contents);
        HttpEntity<String> entity = new HttpEntity<>(jsonRequest, headers);

        String url = geminiApiUrl + "?key=" + geminiApiKey;
        String rawResponse = restTemplate.postForObject(url, entity, String.class);

        JsonNode root = objectMapper.readTree(rawResponse);
        return root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
    }

    private AIChatResponse generateMockResponse(String query) {
        String lower = query.toLowerCase();
        
        if (lower.contains("pay") || lower.contains("salary") || lower.contains("employer") || lower.contains("job") || lower.contains("work")) {
            return AIChatResponse.builder()
                    .responseText("Based on your query regarding employment disputes, it appears you are dealing with unpaid compensation or wage issues. Under employment standards, employers are legally obligated to disburse wages on agreed schedules. *Disclaimer: This information is for education purposes and not legal counsel.*")
                    .category("Labor Law")
                    .checklists(Arrays.asList(
                            "Review your signed Employment Contract for clauses regarding salary and pay frequency.",
                            "Draft a formal written demand letter to your HR department or supervisor.",
                            "Log and document all unpaid hours, timesheets, and emails.",
                            "File a wage claim with your local Labor Commissioner or Department of Labor if unresolved."
                    ))
                    .requiredDocuments(Arrays.asList(
                            "Signed Employment Agreement",
                            "Recent pay slips or bank account statements showing transaction history",
                            "Written notifications, emails, or texts to/from your employer concerning unpaid hours",
                            "Log of worked hours/timesheets"
                    ))
                    .explainedTerms(Arrays.asList(
                            "Wage Claim: A formal complaint filed by an employee against an employer for unpaid wages.",
                            "Employment Standards: Legislative rules defining minimum wage, maximum hours, and payment terms."
                    ))
                    .build();
        } else if (lower.contains("rent") || lower.contains("landlord") || lower.contains("tenant") || lower.contains("lease") || lower.contains("apartment")) {
            return AIChatResponse.builder()
                    .responseText("It appears you are describing a residential tenancy issue. Landlords and tenants both hold reciprocal responsibilities under housing acts (such as maintaining habitable conditions and timely rent payments). *Disclaimer: This is informational only.*")
                    .category("Real Estate Law")
                    .checklists(Arrays.asList(
                            "Verify the clauses in your Lease Agreement concerning maintenance, security deposits, and notices.",
                            "Send a dated, written notice to your landlord highlighting the specific issue.",
                            "Document conditions with high-quality photographs or video recordings.",
                            "Consult local housing tribunal guidelines."
                    ))
                    .requiredDocuments(Arrays.asList(
                            "Lease Agreement / Rental Contract",
                            "Written correspondences with the landlord (emails/letters)",
                            "Photographs of maintenance issues or receipts of payments",
                            "Receipt of Security Deposit"
                    ))
                    .explainedTerms(Arrays.asList(
                            "Warranty of Habitability: The legal requirement that a landlord keep a rental unit fit for human occupation.",
                            "Lease Covenant: A binding promise in a rental contract."
                    ))
                    .build();
        } else if (lower.contains("divorce") || lower.contains("child") || lower.contains("custody") || lower.contains("marriage") || lower.contains("wife") || lower.contains("husband")) {
            return AIChatResponse.builder()
                    .responseText("You are asking about domestic or matrimonial relations. Family law procedures govern separations, parenting plans, and property distributions. *Disclaimer: Consult a licensed family attorney.*")
                    .category("Family Law")
                    .checklists(Arrays.asList(
                            "Gather records of personal assets, liabilities, and shared marital properties.",
                            "Draft a outline of desired child custody arrangements and support expectations.",
                            "Avoid verbal disputes; keep written records of communications regarding child-rearing.",
                            "Engage a qualified family law mediator before filing court petitions."
                    ))
                    .requiredDocuments(Arrays.asList(
                            "Marriage Certificate",
                            "Tax filings for the past 3 years",
                            "Bank statements, mortgage records, and investment statements",
                            "Pre-nuptial or post-nuptial agreements"
                    ))
                    .explainedTerms(Arrays.asList(
                            "Joint Custody: A legal arrangement where both parents share decision-making regarding the child.",
                            "Equitable Distribution: The judicial division of marital assets during divorce procedures."
                    ))
                    .build();
        } else {
            return AIChatResponse.builder()
                    .responseText("I have analyzed your request. To help you proceed, here are standard legal checklist items and necessary document preparations for general disputes. *Disclaimer: This is for educational purposes.*")
                    .category("General")
                    .checklists(Arrays.asList(
                            "Identify the core agreement, verbal commitment, or incident at the center of the dispute.",
                            "Create a timeline of events leading up to the current issue.",
                            "Check if there are any applicable deadlines or Statutes of Limitations for this claim."
                    ))
                    .requiredDocuments(Arrays.asList(
                            "Any agreements, receipts, or contracts signed",
                            "Emails, messages, or letters between parties",
                            "Witness details or physical evidence logs"
                    ))
                    .explainedTerms(Arrays.asList(
                            "Statute of Limitations: The legal deadline within which a lawsuit must be filed.",
                            "Breach of Contract: A failure to perform any promise that forms a part of a contract."
                    ))
                    .build();
        }
    }

    private String generateMockSummary(String text) {
        return "### [MOCK] AI Document Summary\n\n" +
                "**Document Overview:**\n" +
                "This document appears to be a legal agreement or contract (e.g., NDA, Employment, or Lease Agreement).\n\n" +
                "**Key Terms Summary:**\n" +
                "- **Parties**: Identified in the first section of the document.\n" +
                "- **Core Terms**: 1-year duration or standard notification periods.\n" +
                "- **Obligations**: Financial compensation, services to perform, or property maintenance requirements.\n\n" +
                "**Potential Red Flags identified:**\n" +
                "- Vague termination clauses requiring notice but lacking specific breach definitions.\n" +
                "- Ambiguity regarding governing law and dispute resolution mechanisms.\n" +
                "*(Please configure your GEMINI_API_KEY in the .env file to enable complete automated summaries.)*";
    }

    private List<LawyerDTO> getRecommendations(String categoryName) {
        Optional<Category> categoryOpt = categoryRepository.findByName(categoryName);
        List<LawyerProfile> lawyers;

        if (categoryOpt.isPresent()) {
            lawyers = lawyerProfileRepository.findByCategoryAndIsApproved(categoryOpt.get(), true);
        } else {
            lawyers = lawyerProfileRepository.findByIsApproved(true);
        }

        // Limit to top 3 recommendations
        return lawyers.stream()
                .limit(3)
                .map(p -> {
                    double avgRating = reviewRepository.getAverageRatingForLawyer(p);
                    return LawyerDTO.builder()
                            .id(p.getId())
                            .userId(p.getUser().getId())
                            .name(p.getUser().getName())
                            .email(p.getUser().getEmail())
                            .specialization(p.getSpecialization())
                            .bio(p.getBio())
                            .experienceYears(p.getExperienceYears())
                            .hourlyRate(p.getHourlyRate())
                            .categoryId(p.getCategory() != null ? p.getCategory().getId() : null)
                            .categoryName(p.getCategory() != null ? p.getCategory().getName() : "General")
                            .isApproved(p.isApproved())
                            .averageRating(avgRating)
                            .build();
                })
                .collect(Collectors.toList());
    }
}
