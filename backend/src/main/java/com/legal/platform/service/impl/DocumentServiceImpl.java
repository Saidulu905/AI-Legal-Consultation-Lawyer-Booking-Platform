package com.legal.platform.service.impl;

import com.legal.platform.model.Document;
import com.legal.platform.model.User;
import com.legal.platform.repository.DocumentRepository;
import com.legal.platform.repository.UserRepository;
import com.legal.platform.service.AIService;
import com.legal.platform.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final AIService aiService;

    private static final String UPLOAD_DIR = "uploads";

    @Override
    @Transactional
    public Document uploadDocument(Long clientId, MultipartFile file) {
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        try {
            // Create uploads directory if not exists
            Path uploadPath = Paths.get(UPLOAD_DIR).toAbsolutePath();
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Save file with unique name
            String originalFileName = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            Path filePath = uploadPath.resolve(uniqueFileName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Construct accessible URL path
            String fileUrl = "/uploads/" + uniqueFileName;

            // Extract file text mock or read plain text
            String textContent = "Uploaded document: " + originalFileName + " (" + file.getSize() + " bytes).";
            if (originalFileName != null && originalFileName.endsWith(".txt")) {
                textContent = new String(file.getBytes());
            }

            // Generate AI Summary
            String summary = aiService.summarizeDocument(textContent);

            Document document = Document.builder()
                    .client(client)
                    .fileName(originalFileName)
                    .filePath(fileUrl)
                    .summary(summary)
                    .build();

            return documentRepository.save(document);

        } catch (Exception e) {
            throw new RuntimeException("Could not store file. Error: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Document> getClientDocuments(Long clientId) {
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        return documentRepository.findByClientOrderByCreatedAtDesc(client);
    }

    @Override
    @Transactional
    public void deleteDocument(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        try {
            // Delete actual file from disk
            String fileName = document.getFilePath().substring(document.getFilePath().lastIndexOf("/") + 1);
            Path filePath = Paths.get(UPLOAD_DIR).toAbsolutePath().resolve(fileName);
            Files.deleteIfExists(filePath);
        } catch (Exception e) {
            // Log warning but continue deleting DB record
            System.err.println("Could not delete file from disk: " + e.getMessage());
        }

        documentRepository.delete(document);
    }
}
