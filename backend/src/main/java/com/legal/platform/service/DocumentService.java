package com.legal.platform.service;

import com.legal.platform.model.Document;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface DocumentService {
    Document uploadDocument(Long clientId, MultipartFile file);
    List<Document> getClientDocuments(Long clientId);
    void deleteDocument(Long documentId);
}
